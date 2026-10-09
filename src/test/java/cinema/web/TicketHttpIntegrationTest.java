package cinema.web;

import cinema.application.common.interfaces.*;
import cinema.domain.customer.Customer;
import cinema.domain.cinema.*;
import cinema.domain.screening.Screening;
import cinema.domain.common.Money;
import com.fasterxml.jackson.databind.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("venue-dev")
class TicketHttpIntegrationTest {
    @Autowired MockMvc http;
    @Autowired ObjectMapper json;
    @Autowired CustomerRepository customers;
    @Autowired SeatRepository seats;
    @Autowired ScreeningRepository screenings;
    @Autowired SeatReservationRepository reservations;
    @Autowired SeatHoldService holds;
    String owner, other, customer, screening, seat, secondSeat;
    @BeforeEach void setup() {
        String suffix=UUID.randomUUID().toString();
        owner="u-"+suffix; other="other-"+suffix; customer="c-"+suffix;
        screening="screen-"+suffix; seat="seat-"+suffix; secondSeat="second-"+suffix;
        customers.save(new Customer(customer,owner,"Owner"));
        customers.save(new Customer("other-c-"+suffix,other,"Other"));
        seats.save(new Seat(seat,"hall-"+suffix,"A",1,SeatType.NORMAL));
        seats.save(new Seat(secondSeat,"hall-"+suffix,"A",2,SeatType.NORMAL));
        screenings.save(new Screening(screening,"movie-"+suffix,"hall-"+suffix,
                Instant.parse("2030-01-01T10:00:00Z"),Instant.parse("2030-01-01T12:00:00Z"),
                new Money(BigDecimal.TEN,Currency.getInstance("USD"))));
    }
    String cartBody(String id) throws Exception { return json.writeValueAsString(Map.of("screeningId",screening,"seatId",id)); }
    String checkout() throws Exception {
        for (String id: List.of(seat,secondSeat)) {
            http.perform(post("/carts/seats").with(user(owner)).contentType(MediaType.APPLICATION_JSON).content(cartBody(id)))
                    .andExpect(status().isNoContent());
        }
        var response=http.perform(post("/bookings/checkout").with(user(owner))).andExpect(status().isOk()).andReturn();
        return json.readTree(response.getResponse().getContentAsString()).required("bookingIds").get(0).asText();
    }
    @Test void checkoutPayConfirmAndReadTicketsThroughHttp() throws Exception {
        http.perform(get("/screenings/"+screening+"/available-seats"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].available").value(true));
        String booking=checkout();
        http.perform(get("/screenings/"+screening+"/available-seats"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].available").value(false));
        http.perform(get("/tickets/me").with(user(owner))).andExpect(status().isOk()).andExpect(content().json("[]"));
        String paid=http.perform(post("/payments/"+booking).with(user(owner))).andExpect(status().isOk())
                .andExpect(jsonPath("$.successful").value(true)).andReturn().getResponse().getContentAsString();
        http.perform(post("/payments/"+booking).with(user(owner))).andExpect(status().isOk()).andExpect(content().json(paid));
        http.perform(get("/bookings/"+booking).with(user(owner))).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CONFIRMED"));
        var response=http.perform(get("/tickets/me").with(user(owner))).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2)).andReturn();
        JsonNode issued=json.readTree(response.getResponse().getContentAsString());
        Set<String> ticketSeats=new HashSet<>();
        for (JsonNode ticket: issued) {
            assertEquals(booking,ticket.required("bookingId").asText());
            assertEquals("VALID",ticket.required("status").asText());
            assertFalse(ticket.required("ticketCode").asText().isBlank());
            ticketSeats.add(ticket.required("seatId").asText());
            http.perform(get("/tickets/"+ticket.required("id").asText()).with(user(owner)))
                    .andExpect(status().isOk()).andExpect(content().json(ticket.toString()));
        }
        assertEquals(Set.of(seat,secondSeat),ticketSeats);
        http.perform(post("/bookings/"+booking+"/cancel").with(user(owner))).andExpect(status().isConflict());
        holds.release(customer,screening,seat);
        assertTrue(reservations.isBooked(screening,seat));
        http.perform(get("/screenings/"+screening+"/available-seats"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].available").value(false));
        http.perform(post("/carts/seats").with(user(other)).contentType(MediaType.APPLICATION_JSON).content(cartBody(seat)))
                .andExpect(status().isConflict());
    }
    @Test void crossCustomerAndAnonymousAccessAreForbidden() throws Exception {
        String booking=checkout();
        http.perform(post("/payments/"+booking).with(user(other))).andExpect(status().isForbidden());
        http.perform(post("/bookings/"+booking+"/cancel").with(user(other))).andExpect(status().isForbidden());
        http.perform(get("/bookings/"+booking).with(user(other))).andExpect(status().isForbidden());
        http.perform(get("/bookings/me").with(user(other))).andExpect(status().isOk()).andExpect(content().json("[]"));
        http.perform(post("/payments/"+booking).with(user(owner))).andExpect(status().isOk());
        var response=http.perform(get("/tickets/me").with(user(owner))).andExpect(status().isOk()).andReturn();
        String ticket=json.readTree(response.getResponse().getContentAsString()).get(0).required("id").asText();
        http.perform(get("/tickets/"+ticket).with(user(other))).andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("FORBIDDEN"));
        http.perform(get("/tickets/me").with(user(other))).andExpect(status().isOk()).andExpect(content().json("[]"));
        http.perform(post("/payments/"+booking).with(user(other))).andExpect(status().isForbidden());
        for (String path: List.of("/tickets/me","/tickets/"+ticket,"/bookings/me","/bookings/"+booking)) {
            http.perform(get(path)).andExpect(status().isForbidden());
        }
        for (String path: List.of("/payments/"+booking,"/bookings/"+booking+"/cancel","/bookings/checkout")) {
            http.perform(post(path)).andExpect(status().isForbidden());
        }
    }
    @Test void missingResourcesAndCancelledScreening() throws Exception {
        http.perform(get("/tickets/missing").with(user(owner))).andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("NOT_FOUND"));
        http.perform(get("/screenings/missing/available-seats")).andExpect(status().isNotFound());
        http.perform(post("/screenings/"+screening+"/cancel")).andExpect(status().isNoContent());
        http.perform(get("/screenings/"+screening+"/available-seats")).andExpect(status().isOk()).andExpect(jsonPath("$[0].available").value(false));
    }
}

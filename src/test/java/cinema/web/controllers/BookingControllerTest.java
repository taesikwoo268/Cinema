package cinema.web.controllers;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import cinema.application.bookings.commands.CancelBookingCommand;
import cinema.application.bookings.commands.CancelBookingCommandHandler;
import cinema.application.bookings.commands.CheckoutCartCommand;
import cinema.application.bookings.commands.CheckoutCartCommandHandler;
import cinema.application.bookings.queries.GetBookingQuery;
import cinema.application.bookings.queries.GetBookingQueryHandler;
import cinema.application.bookings.queries.GetMyBookingsQuery;
import cinema.application.bookings.queries.GetMyBookingsQueryHandler;
import cinema.application.common.models.BookingView;
import cinema.application.common.models.CheckoutResult;

class BookingControllerTest {

    private final CheckoutCartCommandHandler checkout =
            mock(CheckoutCartCommandHandler.class);

    private final CancelBookingCommandHandler cancel =
            mock(CancelBookingCommandHandler.class);

    private final GetMyBookingsQueryHandler getMy =
            mock(GetMyBookingsQueryHandler.class);

    private final GetBookingQueryHandler get =
            mock(GetBookingQueryHandler.class);

    private final BookingController controller =
            new BookingController(checkout, cancel, getMy, get);

    private MockMvc http;

    @BeforeEach
    void setup() {
        http = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void checkoutDelegatesAndReturnsResult() throws Exception {
        CheckoutResult result =
                new CheckoutResult(List.of("booking-1", "booking-2"));

        when(checkout.handle(new CheckoutCartCommand()))
                .thenReturn(result);

        http.perform(post("/bookings/checkout"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {
                          "bookingIds": ["booking-1", "booking-2"]
                        }
                        """, true));

        verify(checkout).handle(new CheckoutCartCommand());
        verifyNoMoreInteractions(checkout);
        verifyNoInteractions(cancel, getMy, get);
    }

    @Test
    void cancelMapsPathAndReturnsNoContent() throws Exception {
        http.perform(post("/bookings/booking-7/cancel"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(cancel).handle(new CancelBookingCommand("booking-7"));
        verifyNoMoreInteractions(cancel);
        verifyNoInteractions(checkout, getMy, get);
    }

    @Test
    void getMyBookingsReturnsHandlerViews() throws Exception {
        BookingView first = new BookingView(
                "booking-1",
                "screening-1",
                "PENDING",
                new BigDecimal("100.00"),
                "USD");

        BookingView second = new BookingView(
                "booking-2",
                "screening-2",
                "CONFIRMED",
                new BigDecimal("25000"),
                "VND");

        when(getMy.handle(new GetMyBookingsQuery()))
                .thenReturn(List.of(first, second));

        http.perform(get("/bookings/me"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        [
                          {
                            "id": "booking-1",
                            "screeningId": "screening-1",
                            "status": "PENDING",
                            "totalAmount": 100.00,
                            "currency": "USD"
                          },
                          {
                            "id": "booking-2",
                            "screeningId": "screening-2",
                            "status": "CONFIRMED",
                            "totalAmount": 25000,
                            "currency": "VND"
                          }
                        ]
                        """, true));

        verify(getMy).handle(new GetMyBookingsQuery());
        verifyNoMoreInteractions(getMy);
        verifyNoInteractions(checkout, cancel, get);
    }

    @Test
    void getBookingMapsPathAndReturnsHandlerView() throws Exception {
        BookingView view = new BookingView(
                "booking-9",
                "screening-3",
                "CANCELLED",
                new BigDecimal("75.50"),
                "EUR");

        when(get.handle(new GetBookingQuery("booking-9")))
                .thenReturn(view);

        http.perform(get("/bookings/booking-9"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {
                          "id": "booking-9",
                          "screeningId": "screening-3",
                          "status": "CANCELLED",
                          "totalAmount": 75.50,
                          "currency": "EUR"
                        }
                        """, true));

        verify(get).handle(new GetBookingQuery("booking-9"));
        verifyNoMoreInteractions(get);
        verifyNoInteractions(checkout, cancel, getMy);
    }
}
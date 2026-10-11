package cinema.infrastructure.persistence;

import cinema.web.CinemaApplication;
import cinema.application.common.interfaces.*;
import cinema.application.common.models.*;
import cinema.application.common.exceptions.*;
import cinema.application.bookings.commands.*;
import cinema.application.carts.commands.*;
import cinema.application.payments.commands.*;
import cinema.application.tickets.queries.*;
import cinema.domain.cinema.*;
import cinema.domain.movie.*;
import cinema.domain.customer.Customer;
import cinema.domain.screening.Screening;
import cinema.domain.booking.*;
import cinema.domain.cart.*;
import cinema.domain.common.Money;
import cinema.domain.ticket.*;
import cinema.infrastructure.payment.FakePaymentGateway;
import cinema.infrastructure.persistence.adapters.JpaTicketRepositoryAdapter;
import cinema.infrastructure.persistence.adapters.JpaCustomerRepositoryAdapter;
import cinema.infrastructure.persistence.adapters.JpaCartRepositoryAdapter;
import org.junit.jupiter.api.*;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class PersistentWorkflowContract {
    ConfigurableApplicationContext app;
    abstract String databaseUrl();
    boolean mysql() { return databaseUrl().startsWith("jdbc:mysql:"); }
    ConfigurableApplicationContext start() {
        return startWithProfiles("mysql");
    }
    ConfigurableApplicationContext startWithProfiles(String... profiles) {
        return new SpringApplicationBuilder(CinemaApplication.class, TestPorts.class).profiles(profiles).run(
                "--server.port=0", "--server.address=127.0.0.1", "--spring.main.banner-mode=off", "--logging.level.root=WARN",
                "--spring.datasource.url=" + databaseUrl(),
                "--spring.datasource.driver-class-name=" + (mysql() ? "com.mysql.cj.jdbc.Driver" : "org.h2.Driver"),
                "--spring.datasource.username=" + (mysql() ? System.getenv().getOrDefault("CINEMA_TEST_MYSQL_USER", "root") : "sa"),
                "--spring.datasource.password=" + (mysql() ? System.getenv().getOrDefault("CINEMA_TEST_MYSQL_PASSWORD", "") : ""),
                "--spring.flyway.placeholders.tableOptions=" + (mysql() ? "ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin" : ""));
    }
    @BeforeAll void open() { app = start(); }
    @AfterAll void close() { if (app != null) app.close(); }
    @BeforeEach void reset() { app.getBean(TestGateway.class).fail = false; app.getBean(TestTickets.class).failAfter = -1; }
    @AfterEach void logout() { SecurityContextHolder.clearContext(); }
    <T> T bean(Class<T> type) { return app.getBean(type); }
    JdbcTemplate sql() { return bean(JdbcTemplate.class); }
    String id() { return UUID.randomUUID().toString(); }
    Money price() { return new Money(new BigDecimal("12.125"), Currency.getInstance("USD")); }
    record Fixture(String cinema, String hall, String seat, String secondSeat, String movie, String screening, String customer, String user) { }
    Fixture seed() {
        var f = new Fixture(id(), id(), id(), id(), id(), id(), id(), id());
        bean(CinemaRepository.class).save(new Cinema(f.cinema(), "Cinema", "Address"));
        bean(HallRepository.class).save(new Hall(f.hall(), f.cinema(), "Hall"));
        bean(SeatRepository.class).save(new Seat(f.seat(), f.hall(), "A", 1, SeatType.NORMAL));
        bean(SeatRepository.class).save(new Seat(f.secondSeat(), f.hall(), "A", 2, SeatType.VIP));
        bean(MovieRepository.class).save(new Movie(f.movie(), "Movie", "Description", 120, "PG", MovieStatus.SHOWING));
        bean(ScreeningRepository.class).save(new Screening(f.screening(), f.movie(), f.hall(),
                Instant.parse("2030-01-01T10:00:00Z"), Instant.parse("2030-01-01T12:00:00Z"), price()));
        bean(CustomerRepository.class).save(new Customer(f.customer(), f.user(), "Customer"));
        authenticate(f.user());
        return f;
    }
    static void authenticate(String userId) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(userId, "", List.of()));
    }
    void add(Fixture f, String seat) { bean(AddSeatToCartCommandHandler.class).handle(new AddSeatToCartCommand(f.screening(), seat)); }
    String checkout(Fixture f) {
        add(f, f.seat()); add(f, f.secondSeat());
        return bean(CheckoutCartCommandHandler.class).handle(new CheckoutCartCommand()).bookingIds().getFirst();
    }
    PaymentResult pay(String id) { return bean(PayBookingCommandHandler.class).handle(new PayBookingCommand(id)); }
    Booking booking(Fixture f, String customer) {
        var b = new Booking(id(), customer, f.screening(), List.of(new BookingItem(f.seat(), price())), price(), BookingStatus.PENDING, Instant.now());
        bean(BookingRepository.class).save(b);
        return b;
    }
    long count(String table, String bookingId) {
        return sql().queryForObject("select count(*) from " + table + " where booking_id = ?", Long.class, bookingId);
    }

    @Test void allAggregatesRoundTripAndPaymentRetryRetainsTicketState() {
        var f = seed();
        assertEquals("Address", bean(CinemaRepository.class).findById(f.cinema()).orElseThrow().getAddress());
        assertEquals(f.cinema(), bean(HallRepository.class).findById(f.hall()).orElseThrow().getCinemaId());
        assertEquals(SeatType.VIP, bean(SeatRepository.class).findById(f.secondSeat()).orElseThrow().getType());
        assertEquals("Description", bean(MovieRepository.class).findById(f.movie()).orElseThrow().getDescription());
        assertEquals(0, price().amount().compareTo(bean(ScreeningRepository.class).findById(f.screening()).orElseThrow().getBasePrice().amount()));
        String booking = checkout(f);
        assertTrue(bean(CartRepository.class).findActiveByCustomerId(f.customer()).orElseThrow().getItems().isEmpty());
        int calls = bean(TestGateway.class).calls.get();
        var paid = pay(booking);
        assertEquals(paid, pay(booking));
        assertEquals(calls + 1, bean(TestGateway.class).calls.get());
        var persisted = bean(BookingRepository.class).findById(booking).orElseThrow();
        assertEquals(BookingStatus.CONFIRMED, persisted.getStatus());
        assertEquals(2, persisted.getItems().size());
        assertEquals(2, count("seat_reservation_records", booking));
        assertEquals(1, count("payment_records", booking));
        assertEquals(2, count("ticket_records", booking));
        var tickets = bean(TicketRepository.class).findByCustomerId(f.customer());
        var used = tickets.getFirst(); used.markUsed(); bean(TicketRepository.class).save(used);
        pay(booking);
        assertEquals(TicketStatus.USED, bean(TicketRepository.class).findById(used.getId()).orElseThrow().getStatus());
        assertFalse(bean(SeatHoldService.class).hold(f.customer(), f.screening(), f.seat()));
    }

    @Test void failedPaymentLeavesPendingBookingWithoutTicketsOrReservations() {
        var f = seed(); String booking = checkout(f); bean(TestGateway.class).fail = true;
        assertFalse(pay(booking).successful());
        assertEquals(BookingStatus.PENDING, bean(BookingRepository.class).findById(booking).orElseThrow().getStatus());
        assertEquals(0, count("ticket_records", booking)); assertEquals(0, count("seat_reservation_records", booking));
        assertEquals(1, count("payment_records", booking));
        bean(TestGateway.class).fail = false;
        assertTrue(pay(booking).successful()); assertEquals(2, count("ticket_records", booking));
    }

    @Test void failureAfterGatewayAndFirstTicketRollsBackEveryDatabaseChange() {
        var f = seed(); String booking = checkout(f); bean(TestTickets.class).failAfter = 1;
        int calls = bean(TestGateway.class).calls.get();
        assertThrows(IllegalStateException.class, () -> pay(booking));
        assertEquals(calls + 1, bean(TestGateway.class).calls.get());
        assertEquals(BookingStatus.PENDING, bean(BookingRepository.class).findById(booking).orElseThrow().getStatus());
        assertEquals(0, count("payment_records", booking)); assertEquals(0, count("ticket_records", booking));
        assertEquals(0, count("seat_reservation_records", booking));
        bean(TestTickets.class).failAfter = -1;
        pay(booking);
        // An uncommitted gateway success cannot be recovered from the DB: the documented failure window.
        assertEquals(calls + 2, bean(TestGateway.class).calls.get());
        assertEquals(2, count("ticket_records", booking));
    }

    @Test void cartAndHoldWritesRollbackTogether() {
        var f = seed();
        var tx = new TransactionTemplate(bean(PlatformTransactionManager.class));
        assertThrows(IllegalStateException.class, () -> tx.execute(status -> {
            add(f, f.seat());
            throw new IllegalStateException("rollback");
        }));
        assertTrue(bean(CartRepository.class).findActiveByCustomerId(f.customer()).isEmpty());
        assertFalse(bean(SeatHoldService.class).isHeld(f.screening(), f.seat()));
    }

    @Test void rawDatabaseWritesCannotDuplicateReservationTicketOrSuccessfulPayment() {
        var f = seed(); String booking = checkout(f); pay(booking);
        assertThrows(DataIntegrityViolationException.class, () -> sql().update(
                "insert into seat_reservation_records(id,screening_id,seat_id,booking_id) values(?,?,?,?)", id(), f.screening(), f.seat(), booking));
        var t = bean(TicketRepository.class).findByCustomerId(f.customer()).getFirst();
        var otherBooking = booking(f, f.customer());
        assertThrows(DataIntegrityViolationException.class, () -> sql().update(
                "insert into ticket_records(id,booking_id,screening_id,seat_id,ticket_code,status) values(?,?,?,?,?,'VALID')",
                id(), booking, f.screening(), t.getSeatId(), "another-code"));
        assertThrows(DataIntegrityViolationException.class, () -> sql().update(
                "insert into ticket_records(id,booking_id,screening_id,seat_id,ticket_code,status) values(?,?,?,?,?,'VALID')",
                id(), otherBooking.getId(), f.screening(), f.seat(), t.getTicketCode()));
        assertThrows(DataIntegrityViolationException.class, () -> sql().update(
                "insert into ticket_records(id,booking_id,screening_id,seat_id,ticket_code,status) values(?,?,?,?,?,'VALID')",
                t.getId(), otherBooking.getId(), f.screening(), f.seat(), "distinct-code"));
        assertThrows(DataIntegrityViolationException.class, () -> sql().update(
                "insert into payment_records(id,booking_id,amount,currency,status,successful_booking_id) values(?,?,1,'USD','SUCCESS',?)", id(), booking, booking));
        assertEquals(2, count("ticket_records", booking));
    }

    Object[] race(Supplier<?> a, Supplier<?> b) throws Exception {
        var start = new CountDownLatch(1);
        try (var pool = Executors.newFixedThreadPool(2)) {
            var first = pool.submit(() -> run(start, a)); var second = pool.submit(() -> run(start, b)); start.countDown();
            return new Object[]{first.get(30, TimeUnit.SECONDS), second.get(30, TimeUnit.SECONDS)};
        }
    }
    Object run(CountDownLatch start, Supplier<?> action) throws Exception {
        assertTrue(start.await(10, TimeUnit.SECONDS));
        try { return action.get(); } catch (RuntimeException ex) { return ex; }
        finally { SecurityContextHolder.clearContext(); }
    }

    @Test void twoApplicationInstancesPaySameBookingOnlyOnce() throws Exception {
        var f = seed(); String booking = checkout(f);
        try (var second = start()) {
            int calls = bean(TestGateway.class).calls.get();
            var results = race(() -> {authenticate(f.user()); return pay(booking);}, () -> {
                authenticate(f.user()); return second.getBean(PayBookingCommandHandler.class).handle(new PayBookingCommand(booking));
            });
            assertInstanceOf(PaymentResult.class, results[0]); assertEquals(results[0], results[1]);
            assertEquals(1, bean(TestGateway.class).calls.get() - calls + second.getBean(TestGateway.class).calls.get());
            assertEquals(2, count("ticket_records", booking)); assertEquals(1, count("payment_records", booking));
        }
    }

    @Test void twoApplicationInstancesCannotPayDifferentBookingsForSameSeat() throws Exception {
        var f = seed(); var b1 = booking(f, f.customer());
        String otherCustomer = id(), otherUser = id(); bean(CustomerRepository.class).save(new Customer(otherCustomer, otherUser, "Other"));
        var b2 = booking(f, otherCustomer);
        try (var second = start()) {
            int calls = bean(TestGateway.class).calls.get();
            var results = race(() -> {authenticate(f.user()); return pay(b1.getId());}, () -> {
                authenticate(otherUser); return second.getBean(PayBookingCommandHandler.class).handle(new PayBookingCommand(b2.getId()));
            });
            assertEquals(1, Arrays.stream(results).filter(PaymentResult.class::isInstance).count(), Arrays.toString(results));
            assertEquals(1, Arrays.stream(results).filter(ConflictException.class::isInstance).count(), Arrays.toString(results));
            assertEquals(1, bean(TestGateway.class).calls.get() - calls + second.getBean(TestGateway.class).calls.get());
            assertEquals(1, count("ticket_records", b1.getId()) + count("ticket_records", b2.getId()));
            assertEquals(1, count("seat_reservation_records", b1.getId()) + count("seat_reservation_records", b2.getId()));
        }
    }

    @Test void twoInstancesCreateOneCartAndConsumeItOnlyOnce() throws Exception {
        var f = seed();
        try (var second = start()) {
            var added = race(() -> {authenticate(f.user()); add(f, f.seat()); return "added";}, () -> {
                authenticate(f.user()); second.getBean(AddSeatToCartCommandHandler.class).handle(new AddSeatToCartCommand(f.screening(), f.secondSeat())); return "added";
            });
            assertArrayEquals(new Object[]{"added", "added"}, added);
            var checked = race(() -> {authenticate(f.user()); return bean(CheckoutCartCommandHandler.class).handle(new CheckoutCartCommand());}, () -> {
                authenticate(f.user()); return second.getBean(CheckoutCartCommandHandler.class).handle(new CheckoutCartCommand());
            });
            assertEquals(1, Arrays.stream(checked).filter(CheckoutResult.class::isInstance).count(), Arrays.toString(checked));
            assertEquals(1, Arrays.stream(checked).filter(ValidationException.class::isInstance).count(), Arrays.toString(checked));
            var saved = bean(BookingRepository.class).findByCustomerId(f.customer()); assertEquals(1, saved.size()); assertEquals(2, saved.getFirst().getItems().size());
        }
    }

    @Test void fullHttpCheckoutPaymentTicketsUsePersistentRepositories() throws Exception {
        var f = seed();
        var mvc = MockMvcBuilders.webAppContextSetup((WebApplicationContext) app).apply(springSecurity()).build();
        var account = registerAndLogin(mvc);
        String json = "{\"screeningId\":\"" + f.screening() + "\",\"seatId\":\"" + f.seat() + "\"}";
        mvc.perform(post("/carts/seats").header("Authorization", account.bearer()).contentType("application/json").content(json)).andExpect(status().isNoContent());
        String body = mvc.perform(post("/bookings/checkout").header("Authorization", account.bearer())).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String booking = bean(ObjectMapper.class).readTree(body).get("bookingIds").get(0).asText();
        int calls = bean(TestGateway.class).calls.get();
        mvc.perform(post("/payments/" + booking).header("Authorization", account.bearer())).andExpect(status().isOk()).andExpect(jsonPath("$.successful").value(true));
        mvc.perform(post("/payments/" + booking).header("Authorization", account.bearer())).andExpect(status().isOk()).andExpect(jsonPath("$.successful").value(true));
        assertEquals(calls + 1, bean(TestGateway.class).calls.get());
        assertEquals(1, count("payment_records", booking));
        mvc.perform(get("/bookings/" + booking).header("Authorization", account.bearer())).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CONFIRMED"));
        mvc.perform(get("/tickets/me").header("Authorization", account.bearer())).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        SecurityContextHolder.clearContext();
        mvc.perform(get("/tickets/me").with(anonymous())).andExpect(status().isForbidden());
        assertEquals(1, count("ticket_records", booking));
    }

    record HttpAccount(String email, String password, String userId, String customerId, String token) {
        String bearer() { return "Bearer " + token; }
    }

    org.springframework.test.web.servlet.MockMvc http(ConfigurableApplicationContext context) {
        return MockMvcBuilders.webAppContextSetup((WebApplicationContext) context).apply(springSecurity()).build();
    }

    HttpAccount registerAndLogin(org.springframework.test.web.servlet.MockMvc mvc) throws Exception {
        SecurityContextHolder.clearContext();
        String email = id() + "@example.com", password = id();
        var mapper = bean(ObjectMapper.class);
        String registered = mvc.perform(post("/identity/register").contentType("application/json")
                        .content(mapper.writeValueAsString(Map.of("email", email, "password", password, "displayName", "Customer", "role", "ADMIN"))))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String loggedIn = mvc.perform(post("/identity/login").contentType("application/json")
                        .content(mapper.writeValueAsString(Map.of("email", email, "password", password))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String userId = sql().queryForObject("select id from user_records where email=?", String.class, email);
        assertEquals("CUSTOMER", sql().queryForObject("select role from user_records where id=?", String.class, userId));
        return new HttpAccount(email, password, userId, mapper.readTree(registered).get("id").asText(),
                mapper.readTree(loggedIn).get("accessToken").asText());
    }

    @Test void bearerTokensAreHashedAndSurviveRestartAndAnotherApplicationInstance() throws Exception {
        var account = registerAndLogin(http(app));
        String stored = sql().queryForObject("select token_hash from access_token_records where user_id=?", String.class, account.userId());
        assertEquals(64, stored.length()); assertNotEquals(account.token(), stored);
        assertEquals(java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256")
                .digest(account.token().getBytes(java.nio.charset.StandardCharsets.US_ASCII))), stored);
        try (var second = start()) {
            http(second).perform(get("/identity/me").header("Authorization", account.bearer()))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.userId").value(account.userId()));
        }
        app.close(); app = start();
        http(app).perform(get("/identity/me").header("Authorization", account.bearer()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.userId").value(account.userId()));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        http(app).perform(get("/identity/me")).andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test void invalidExpiredAndDuplicateBearerHeadersAreRejectedWithoutSessionFallback() throws Exception {
        var mvc = http(app); var account = registerAndLogin(mvc);
        for (String header : List.of("Basic abc", "Bearer ", "Bearer invalid", "Bearer " + "x".repeat(43),
                account.bearer() + " ", account.bearer() + ", " + account.bearer())) {
            mvc.perform(get("/identity/me").header("Authorization", header))
                    .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("FORBIDDEN"));
        }
        mvc.perform(get("/identity/me").header("Authorization", account.bearer(), account.bearer())).andExpect(status().isForbidden());
        mvc.perform(get("/identity/me").header("Authorization", account.bearer()))
                .andExpect(status().isOk()).andExpect(cookie().doesNotExist("JSESSIONID"));
        sql().update("update access_token_records set expires_at=? where user_id=?",
                java.sql.Timestamp.from(Instant.parse("2000-01-01T00:00:00Z")), account.userId());
        mvc.perform(get("/identity/me").header("Authorization", account.bearer())).andExpect(status().isForbidden());
        mvc.perform(post("/identity/login").contentType("application/json").content(bean(ObjectMapper.class)
                .writeValueAsString(Map.of("email", account.email(), "password", "incorrect"))))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.message").value("Invalid credentials"));
        assertEquals(1L, sql().queryForObject("select count(*) from access_token_records where user_id=?", Long.class, account.userId()));
    }

    @Test void customerCannotManageCatalogAndDatabaseRoleChangesApplyToExistingTokens() throws Exception {
        var mvc = http(app); var account = registerAndLogin(mvc);
        for (String route : List.of("/cinemas", "/halls", "/seats", "/movies", "/screenings", "/screenings/absent/cancel")) {
            mvc.perform(post(route).header("Authorization", account.bearer()).contentType("application/json").content("{}"))
                    .andExpect(status().isForbidden());
            mvc.perform(post(route).contentType("application/json").content("{}"))
                    .andExpect(status().isForbidden());
        }
        mvc.perform(delete("/movies/absent").header("Authorization", account.bearer())).andExpect(status().isForbidden());
        mvc.perform(get("/movies")).andExpect(status().isOk());
        sql().update("update user_records set role='ADMIN' where id=?", account.userId());
        mvc.perform(post("/cinemas").header("Authorization", account.bearer()).contentType("application/json")
                .content("{\"name\":\"Admin cinema\",\"address\":\"Address\"}"))
                .andExpect(status().isCreated());
        sql().update("update user_records set role='CUSTOMER' where id=?", account.userId());
        mvc.perform(post("/cinemas").header("Authorization", account.bearer()).contentType("application/json")
                .content("{\"name\":\"Denied\",\"address\":\"Address\"}"))
                .andExpect(status().isForbidden());
    }

    @Test void bearerOwnershipChecksAlsoApplyToAdmins() throws Exception {
        var f = seed(); var mvc = http(app); var owner = registerAndLogin(mvc); var other = registerAndLogin(mvc);
        var b = booking(f, owner.customerId());
        var ticket = new Ticket(id(), b.getId(), f.screening(), f.seat(), id(), TicketStatus.VALID);
        bean(TicketRepository.class).save(ticket);
        for (String role : List.of("CUSTOMER", "ADMIN")) {
            sql().update("update user_records set role=? where id=?", role, other.userId());
            mvc.perform(get("/bookings/" + b.getId()).header("Authorization", other.bearer())).andExpect(status().isForbidden());
            mvc.perform(post("/bookings/" + b.getId() + "/cancel").header("Authorization", other.bearer())).andExpect(status().isForbidden());
            mvc.perform(post("/payments/" + b.getId()).header("Authorization", other.bearer())).andExpect(status().isForbidden());
            mvc.perform(get("/tickets/" + ticket.getId()).header("Authorization", other.bearer())).andExpect(status().isForbidden());
            mvc.perform(get("/tickets/me").header("Authorization", other.bearer())).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        }
        assertEquals(BookingStatus.PENDING, bean(BookingRepository.class).findById(b.getId()).orElseThrow().getStatus());
        assertEquals(0, count("payment_records", b.getId()));
        mvc.perform(get("/tickets/" + ticket.getId()).header("Authorization", owner.bearer())).andExpect(status().isOk());
    }

    String createWithToken(org.springframework.test.web.servlet.MockMvc mvc, HttpAccount account,
            String route, Map<String, ?> input) throws Exception {
        var response = mvc.perform(post(route).header("Authorization", account.bearer())
                        .contentType("application/json").content(bean(ObjectMapper.class).writeValueAsString(input)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return bean(ObjectMapper.class).readTree(response).get("id").asText();
    }

    @Test void adminCanManageEveryCatalogMutationThroughBearerAuthentication() throws Exception {
        var mvc = http(app); var admin = registerAndLogin(mvc);
        sql().update("update user_records set role='ADMIN' where id=?", admin.userId());
        String cinema = createWithToken(mvc, admin, "/cinemas", Map.of("name", "Cinema", "address", "Address"));
        String hall = createWithToken(mvc, admin, "/halls", Map.of("cinemaId", cinema, "name", "Hall"));
        createWithToken(mvc, admin, "/seats", Map.of("hallId", hall, "row", "A", "number", 1, "type", "NORMAL"));
        String movie = createWithToken(mvc, admin, "/movies", Map.of("title", "Movie", "description", "Description", "durationMinutes", 120, "ageRating", "PG"));
        String screening = createWithToken(mvc, admin, "/screenings", Map.of("movieId", movie, "hallId", hall,
                "startTime", "2030-01-01T10:00:00Z", "endTime", "2030-01-01T12:00:00Z", "basePrice", 12, "currency", "USD"));
        mvc.perform(post("/screenings/" + screening + "/cancel").header("Authorization", admin.bearer())).andExpect(status().isNoContent());
        mvc.perform(delete("/movies/" + movie).header("Authorization", admin.bearer())).andExpect(status().isNoContent());
        mvc.perform(get("/movies/" + movie)).andExpect(status().isNotFound());
        mvc.perform(get("/unimplemented").header("Authorization", admin.bearer())).andExpect(status().isForbidden());
    }

    @Test void registrationRollbackRemovesUserAndCustomerTogether() {
        String email = id() + "@example.com";
        var tx = new TransactionTemplate(bean(PlatformTransactionManager.class));
        String[] userId = new String[1];
        assertThrows(IllegalStateException.class, () -> tx.execute(status -> {
            bean(cinema.application.identity.commands.RegisterCommandHandler.class).handle(
                    new cinema.application.identity.commands.RegisterCommand(email, id(), "Rollback"));
            userId[0] = sql().queryForObject("select id from user_records where email=?", String.class, email);
            throw new IllegalStateException("rollback");
        }));
        assertEquals(0L, sql().queryForObject("select count(*) from user_records where email=?", Long.class, email));
        assertEquals(0L, sql().queryForObject("select count(*) from customer_records where user_id=?", Long.class, userId[0]));
    }

    @Test void failedLoginTransactionDoesNotLeaveUsableToken() throws Exception {
        var account = registerAndLogin(http(app));
        var tx = new TransactionTemplate(bean(PlatformTransactionManager.class));
        String[] rolledBackToken = new String[1];
        assertThrows(IllegalStateException.class, () -> tx.execute(status -> {
            rolledBackToken[0] = bean(cinema.application.identity.commands.LoginCommandHandler.class).handle(
                    new cinema.application.identity.commands.LoginCommand(account.email(), account.password())).accessToken();
            throw new IllegalStateException("rollback after token flush");
        }));
        assertTrue(bean(TokenVerifier.class).verify(rolledBackToken[0]).isEmpty());
        assertTrue(bean(TokenVerifier.class).verify(account.token()).isPresent());
        assertEquals(1L, sql().queryForObject("select count(*) from access_token_records where user_id=?", Long.class, account.userId()));
    }

    @Test void migrationFromPackage2PreservesExistingAccountsAndDefaultsToCustomer() {
        String schema = "auth_upgrade_" + id().replace("-", "");
        var source = bean(javax.sql.DataSource.class);
        var options = Map.of("tableOptions", mysql() ? "ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin" : "");
        org.flywaydb.core.Flyway.configure().dataSource(source).schemas(schema).defaultSchema(schema)
                .placeholders(options).target("2").load().migrate();
        String userId = id();
        sql().update("insert into " + schema + ".user_records(id,email,password_hash) values(?,?,?)", userId, "legacy@example.com", "existing-hash");
        sql().update("insert into " + schema + ".customer_records(id,user_id,display_name) values(?,?,?)", id(), userId, "Existing customer");
        org.flywaydb.core.Flyway.configure().dataSource(source).schemas(schema).defaultSchema(schema)
                .placeholders(options).load().migrate();
        assertEquals("CUSTOMER", sql().queryForObject("select role from " + schema + ".user_records where id=?", String.class, userId));
        assertEquals("existing-hash", sql().queryForObject("select password_hash from " + schema + ".user_records where id=?", String.class, userId));
        assertEquals(1L, sql().queryForObject("select count(*) from " + schema + ".customer_records where user_id=?", Long.class, userId));
        var rejected = assertThrows(org.springframework.dao.DataAccessException.class, () -> sql().update(
                "update " + schema + ".user_records set role='SUPERADMIN' where id=?", userId));
        var cause = assertInstanceOf(java.sql.SQLException.class, rejected.getMostSpecificCause());
        assertEquals(mysql() ? 3819 : 23513, cause.getErrorCode());
        assertEquals("CUSTOMER", sql().queryForObject("select role from " + schema + ".user_records where id=?", String.class, userId));
    }

    @Test void combinedProfilesStillUseMysqlSecurityAndPersistentRepositories() throws Exception {
        try (var combined = startWithProfiles("mysql", "venue-dev")) {
            assertEquals(1, combined.getBeansOfType(org.springframework.security.web.SecurityFilterChain.class).size());
            assertTrue(combined.containsBean("mysqlSecurityFilterChain"));
            assertFalse(combined.containsBean("venueDevSecurityFilterChain"));
            assertFalse(combined.getBean(CartRepository.class).getClass().getName().contains("inmemory"));
            http(combined).perform(post("/cinemas").contentType("application/json")
                    .content("{\"name\":\"Denied\",\"address\":\"Address\"}"))
                    .andExpect(status().isForbidden());
            var account = registerAndLogin(http(combined));
            http(combined).perform(get("/identity/me").header("Authorization", account.bearer()))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.userId").value(account.userId()));
        }
    }

    @Test void applicationRestartRetainsDataAndSuccessfulPaymentPreventsRecharging() {
        var f = seed(); String booking = checkout(f); var paid = pay(booking);
        String email = id() + "@example.com";
        String password = id();
        bean(IdentityService.class).register(email, password);
        app.close(); app = start(); authenticate(f.user());
        assertEquals(BookingStatus.CONFIRMED, bean(BookingRepository.class).findById(booking).orElseThrow().getStatus());
        assertEquals(2, bean(TicketRepository.class).findByCustomerId(f.customer()).size());
        assertTrue(bean(SeatReservationRepository.class).isBooked(f.screening(), f.seat()));
        assertTrue(bean(SeatHoldService.class).isHeldBy(f.customer(), f.screening(), f.seat()));
        assertEquals(paid, pay(booking)); assertEquals(0, bean(TestGateway.class).calls.get());
        assertNotNull(bean(IdentityService.class).login(email, password).accessToken());
    }

    @Test void mysqlProfileContainsNoInMemoryRepositoryAndFlywayOwnsSchema() {
        for (var type : List.of(CinemaRepository.class, HallRepository.class, SeatRepository.class, MovieRepository.class,
                ScreeningRepository.class, CustomerRepository.class, CartRepository.class, BookingRepository.class,
                PaymentRepository.class, SeatReservationRepository.class, SeatHoldService.class)) {
            assertFalse(bean(type).getClass().getName().contains("inmemory"), type.getName());
        }
        assertEquals("validate", app.getEnvironment().getProperty("spring.jpa.hibernate.ddl-auto"));
        assertEquals(1L, sql().queryForObject("select count(*) from flyway_schema_history where version='1' and success=true", Long.class));
        if (mysql()) assertTrue(sql().queryForObject("select version()", String.class).startsWith("9.") || sql().queryForObject("select version()", String.class).startsWith("8."));
    }

    @Test void deletingReferencedMoviePreservesHistoricalScreeningAndTickets() {
        var f = seed(); String booking = checkout(f); pay(booking);
        bean(MovieRepository.class).deleteById(f.movie());
        assertTrue(bean(MovieRepository.class).findById(f.movie()).isEmpty());
        assertTrue(bean(MovieRepository.class).findAll().stream().noneMatch(m -> m.getId().equals(f.movie())));
        assertEquals(f.movie(), bean(ScreeningRepository.class).findById(f.screening()).orElseThrow().getMovieId());
        assertEquals(2, bean(TicketRepository.class).findByCustomerId(f.customer()).size());
    }

    @Test void savingStaleMovieCannotUndoSoftDeletion() {
        var f = seed(); String booking = checkout(f); pay(booking);
        var movies = bean(MovieRepository.class);
        var stale = movies.findById(f.movie()).orElseThrow();
        movies.deleteById(f.movie());
        movies.save(stale);
        assertTrue(movies.findById(f.movie()).isEmpty());
        assertTrue(movies.findAll().stream().noneMatch(m -> m.getId().equals(f.movie())));
        assertEquals(f.movie(), bean(ScreeningRepository.class).findById(f.screening()).orElseThrow().getMovieId());
        assertEquals(2, bean(TicketRepository.class).findByCustomerId(f.customer()).size());
    }

    @Test void everyCommandHandlerHasMysqlTransactionAdvice() {
        var advice = app.getBean("mysqlCommandTransaction");
        var handlers = app.getBeansOfType(UseCase.class).values().stream()
                .filter(b -> org.springframework.aop.support.AopUtils.getTargetClass(b).getSimpleName().endsWith("CommandHandler"))
                .toList();
        assertTrue(handlers.size() >= 15);
        for (var handler : handlers) {
            var advised = assertInstanceOf(org.springframework.aop.framework.Advised.class, handler);
            assertTrue(Arrays.stream(advised.getAdvisors()).anyMatch(a -> a.getAdvice() == advice), handler.getClass().getName());
        }
    }

    @Test void eachCartCommandRollsBackCartHoldsAndBookingsAfterSaveFailure() {
        for (String operation : List.of("add", "remove", "clear", "checkout")) {
            var f = seed(); add(f, f.seat());
            bean(TestCarts.class).fail = true;
            try {
                assertThrows(IllegalStateException.class, () -> cartCommand(app, f, operation));
            } finally { bean(TestCarts.class).fail = false; }
            var items = bean(CartRepository.class).findActiveByCustomerId(f.customer()).orElseThrow().getItems();
            assertEquals(List.of(f.seat()), items.stream().map(CartItem::seatId).toList());
            assertTrue(bean(SeatHoldService.class).isHeldBy(f.customer(), f.screening(), f.seat()));
            assertFalse(bean(SeatHoldService.class).isHeld(f.screening(), f.secondSeat()));
            assertTrue(bean(BookingRepository.class).findByCustomerId(f.customer()).isEmpty());
        }
    }

    Object cartCommand(ConfigurableApplicationContext context, Fixture f, String operation) {
        authenticate(f.user());
        return switch (operation) {
            case "add" -> context.getBean(AddSeatToCartCommandHandler.class).handle(new AddSeatToCartCommand(f.screening(), f.secondSeat()));
            case "remove" -> context.getBean(RemoveSeatFromCartCommandHandler.class).handle(new RemoveSeatFromCartCommand(f.screening(), f.seat()));
            case "clear" -> context.getBean(ClearCartCommandHandler.class).handle(new ClearCartCommand());
            case "checkout" -> context.getBean(CheckoutCartCommandHandler.class).handle(new CheckoutCartCommand());
            default -> throw new AssertionError(operation);
        };
    }

    static void await(CountDownLatch latch) {
        try { assertTrue(latch.await(15, TimeUnit.SECONDS), "Coordination timed out"); }
        catch (InterruptedException ex) { Thread.currentThread().interrupt(); throw new AssertionError(ex); }
    }

    @Test void checkoutSerializesWithEveryCartMutationAcrossContextsInBothOrders() throws Exception {
        try (var second = start()) {
            for (String mutation : List.of("add", "remove", "clear", "checkout")) {
                for (boolean checkoutFirst : List.of(true, false)) {
                    var f = seed(); add(f, f.seat());
                    var locked = new CountDownLatch(1);
                    var release = new CountDownLatch(1);
                    var competing = new CountDownLatch(1);
                    bean(TestCustomers.class).afterLock = () -> {
                        assertTrue(org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive());
                        locked.countDown(); await(release);
                    };
                    second.getBean(TestCustomers.class).beforeLock = competing::countDown;
                    Object firstResult, secondResult;
                    try (var pool = Executors.newFixedThreadPool(2)) {
                        var first = pool.submit(() -> run(new CountDownLatch(0),
                                () -> cartCommand(app, f, checkoutFirst ? "checkout" : mutation)));
                        try {
                            await(locked);
                            var other = pool.submit(() -> run(new CountDownLatch(0),
                                    () -> cartCommand(second, f, checkoutFirst ? mutation : "checkout")));
                            await(competing);
                            release.countDown();
                            firstResult = first.get(30, TimeUnit.SECONDS);
                            secondResult = other.get(30, TimeUnit.SECONDS);
                        } finally {
                            release.countDown();
                            bean(TestCustomers.class).afterLock = () -> {};
                            second.getBean(TestCustomers.class).beforeLock = () -> {};
                        }
                    }
                    assertFalse(firstResult instanceof RuntimeException, String.valueOf(firstResult));
                    var bookings = bean(BookingRepository.class).findByCustomerId(f.customer());
                    var cart = bean(CartRepository.class).findActiveByCustomerId(f.customer()).orElseThrow();
                    if (mutation.equals("add")) {
                        assertFalse(secondResult instanceof RuntimeException, String.valueOf(secondResult));
                        assertEquals(1, bookings.size());
                        assertEquals(checkoutFirst ? 1 : 2, bookings.getFirst().getItems().size());
                        assertEquals(checkoutFirst ? List.of(f.secondSeat()) : List.of(), cart.getItems().stream().map(CartItem::seatId).toList());
                    } else {
                        assertEquals(checkoutFirst || mutation.equals("checkout") ? 1 : 0, bookings.size());
                        assertTrue(cart.getItems().isEmpty());
                        if (mutation.equals("checkout") || !checkoutFirst) assertInstanceOf(ValidationException.class, secondResult);
                        else if (mutation.equals("remove")) assertInstanceOf(NotFoundException.class, secondResult);
                        else assertNull(secondResult);
                    }
                }
            }
        }
    }

    @Test void overlappingScreeningsCannotBeInsertedConcurrently() throws Exception {
        var f = seed();
        var start = Instant.parse("2030-01-01T14:00:00Z");
        try (var second = start()) {
            var results = race(() -> {
                bean(ScreeningRepository.class).save(new Screening(id(), f.movie(), f.hall(), start, start.plusSeconds(3600), price()));
                return "saved";
            }, () -> {
                second.getBean(ScreeningRepository.class).save(new Screening(id(), f.movie(), f.hall(), start, start.plusSeconds(3600), price()));
                return "saved";
            });
            assertEquals(1, Arrays.stream(results).filter("saved"::equals).count(), Arrays.toString(results));
            assertEquals(1, Arrays.stream(results).filter(ConflictException.class::isInstance).count(), Arrays.toString(results));
        }
    }

    @Test void holdAndReservationShareDatabaseLockAcrossInstances() throws Exception {
        var f = seed(); var booking = booking(f, f.customer());
        try (var second = start()) {
            var results = race(() -> bean(SeatHoldService.class).hold(f.customer(), f.screening(), f.seat()), () -> {
                second.getBean(SeatReservationRepository.class).markBooked(f.screening(), f.seat(), booking.getId());
                return "booked";
            });
            assertInstanceOf(Boolean.class, results[0]); assertEquals("booked", results[1]);
            assertTrue(bean(SeatReservationRepository.class).isBooked(f.screening(), f.seat()));
            assertFalse(bean(SeatHoldService.class).hold(f.customer(), f.screening(), f.seat()));
            second.getBean(SeatHoldService.class).release(f.customer(), f.screening(), f.seat());
            assertFalse(second.getBean(SeatHoldService.class).hold(f.customer(), f.screening(), f.seat()));
        }
    }

    @Test void multipleScreeningsCheckoutAsSeparateBookingsAndMissingHoldDoesNotConsumeCart() {
        var f = seed(); String otherScreening = id();
        bean(ScreeningRepository.class).save(new Screening(otherScreening, f.movie(), f.hall(),
                Instant.parse("2030-01-01T13:00:00Z"), Instant.parse("2030-01-01T14:00:00Z"), price()));
        add(f, f.seat());
        bean(AddSeatToCartCommandHandler.class).handle(new AddSeatToCartCommand(otherScreening, f.secondSeat()));
        bean(SeatHoldService.class).release(f.customer(), otherScreening, f.secondSeat());
        assertThrows(ConflictException.class, () -> bean(CheckoutCartCommandHandler.class).handle(new CheckoutCartCommand()));
        assertEquals(2, bean(CartRepository.class).findActiveByCustomerId(f.customer()).orElseThrow().getItems().size());
        assertTrue(bean(BookingRepository.class).findByCustomerId(f.customer()).isEmpty());
        assertTrue(bean(SeatHoldService.class).hold(f.customer(), otherScreening, f.secondSeat()));
        var result = bean(CheckoutCartCommandHandler.class).handle(new CheckoutCartCommand());
        assertEquals(2, result.bookingIds().size());
        result.bookingIds().forEach(this::pay);
        assertEquals(2, bean(TicketRepository.class).findByCustomerId(f.customer()).size());
    }

    @Test void moneyIsNeverSilentlyRoundedByPersistence() {
        var f = seed();
        var tooPrecise = new Money(new BigDecimal("0.0000000000001"), Currency.getInstance("USD"));
        assertThrows(ValidationException.class, () -> bean(ScreeningRepository.class).save(new Screening(id(), f.movie(), f.hall(),
                Instant.parse("2030-01-01T13:00:00Z"), Instant.parse("2030-01-01T14:00:00Z"), tooPrecise)));
        assertEquals(1, bean(ScreeningRepository.class).findByMovieId(f.movie()).size());
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class TestPorts {
        @Bean @Primary TestGateway testGateway() { return new TestGateway(); }
        @Bean @Primary TestTickets testTickets(JpaTicketRepositoryAdapter delegate) { return new TestTickets(delegate); }
        @Bean @Primary TestCustomers testCustomers(JpaCustomerRepositoryAdapter delegate) { return new TestCustomers(delegate); }
        @Bean @Primary TestCarts testCarts(JpaCartRepositoryAdapter delegate) { return new TestCarts(delegate); }
    }
    static class TestCustomers implements CustomerRepository {
        final CustomerRepository delegate;
        volatile Runnable beforeLock = () -> {}, afterLock = () -> {};
        TestCustomers(CustomerRepository delegate) { this.delegate = delegate; }
        public Optional<Customer> findById(String id) { return delegate.findById(id); }
        public Optional<Customer> findByUserId(String id) {
            beforeLock.run();
            var result = delegate.findByUserId(id);
            afterLock.run();
            return result;
        }
        public void save(Customer customer) { delegate.save(customer); }
    }
    static class TestCarts implements CartRepository {
        final CartRepository delegate; volatile boolean fail;
        TestCarts(CartRepository delegate) { this.delegate = delegate; }
        public Optional<Cart> findActiveByCustomerId(String id) { return delegate.findActiveByCustomerId(id); }
        public void save(Cart cart) {
            delegate.save(cart);
            if (fail) throw new IllegalStateException("Failure after cart flush");
        }
    }
    static class TestGateway implements PaymentGateway {
        final FakePaymentGateway delegate = new FakePaymentGateway(); final AtomicInteger calls = new AtomicInteger(); volatile boolean fail;
        public PaymentResult pay(String booking, Money amount) { calls.incrementAndGet(); return fail ? new PaymentResult(false, "declined") : delegate.pay(booking, amount); }
        public PaymentResult refund(String reference, Money amount) { return delegate.refund(reference, amount); }
    }
    static class TestTickets implements TicketRepository {
        final TicketRepository delegate; int failAfter = -1;
        TestTickets(TicketRepository delegate) { this.delegate = delegate; }
        public Optional<Ticket> findById(String id) { return delegate.findById(id); }
        public List<Ticket> findByCustomerId(String id) { return delegate.findByCustomerId(id); }
        public void save(Ticket ticket) { if (failAfter == 0) throw new IllegalStateException("ticket write failed"); if (failAfter > 0) failAfter--; delegate.save(ticket); }
    }
}

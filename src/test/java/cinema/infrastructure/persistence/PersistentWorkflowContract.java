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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class PersistentWorkflowContract {
    ConfigurableApplicationContext app;
    abstract String databaseUrl();
    boolean mysql() { return databaseUrl().startsWith("jdbc:mysql:"); }
    ConfigurableApplicationContext start() {
        return new SpringApplicationBuilder(CinemaApplication.class, TestPorts.class).profiles("mysql").run(
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
        String json = "{\"screeningId\":\"" + f.screening() + "\",\"seatId\":\"" + f.seat() + "\"}";
        mvc.perform(post("/carts/seats").with(user(f.user())).contentType("application/json").content(json)).andExpect(status().isNoContent());
        String body = mvc.perform(post("/bookings/checkout").with(user(f.user()))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String booking = bean(ObjectMapper.class).readTree(body).get("bookingIds").get(0).asText();
        mvc.perform(post("/payments/" + booking).with(user(f.user()))).andExpect(status().isOk()).andExpect(jsonPath("$.successful").value(true));
        mvc.perform(get("/bookings/" + booking).with(user(f.user()))).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CONFIRMED"));
        mvc.perform(get("/tickets/me").with(user(f.user()))).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        SecurityContextHolder.clearContext();
        mvc.perform(get("/tickets/me").with(anonymous())).andExpect(status().isForbidden());
        assertEquals(1, count("ticket_records", booking));
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

package cinema.application.carts.commands;

import cinema.application.bookings.commands.CheckoutCartCommand;
import cinema.application.bookings.commands.CheckoutCartCommandHandler;
import cinema.application.common.exceptions.*;
import cinema.application.common.interfaces.*;
import cinema.application.common.models.CheckoutResult;
import cinema.domain.cart.Cart;
import cinema.domain.cart.CartItem;
import cinema.domain.cinema.Seat;
import cinema.domain.cinema.SeatType;
import cinema.domain.common.Money;
import cinema.domain.customer.Customer;
import cinema.domain.screening.Screening;
import cinema.infrastructure.persistence.inmemory.*;
import java.lang.management.ManagementFactory;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CartMutationConcurrencyTest {
    enum Mutation { ADD, REMOVE, CLEAR }
    private final CurrentUser user = mock(CurrentUser.class);
    private final InMemoryCustomerRepository customers = new InMemoryCustomerRepository();
    private final InMemoryCartRepository storedCarts = new InMemoryCartRepository();
    private final InMemoryBookingRepository bookings = new InMemoryBookingRepository();
    private final ScreeningRepository screenings = mock(ScreeningRepository.class);
    private final SeatRepository seats = mock(SeatRepository.class);
    private final InMemorySeatState seatState = new InMemorySeatState();
    private final SimpleSeatHoldService holds = new SimpleSeatHoldService(seatState);
    private final InMemorySeatReservationRepository reservations = new InMemorySeatReservationRepository(seatState);
    private final Customer customer = new Customer("c", "u", "Customer");
    private final Money price = new Money(BigDecimal.TEN, Currency.getInstance("USD"));
    private final Instant now = Instant.parse("2030-01-01T10:00:00Z");
    private final AtomicInteger cartIds = new AtomicInteger();
    private final CountDownLatch firstSaving = new CountDownLatch(1);
    private final CountDownLatch finishSave = new CountDownLatch(1);
    private final AtomicBoolean pauseNextSave = new AtomicBoolean(true);
    private final IllegalStateException saveFailure = new IllegalStateException("save failed");
    private boolean failFirstSave;

    // The barrier is outside the real repository monitor; it simulates an unfinished write.
    private final CartRepository carts = new CartRepository() {
        @Override public Optional<Cart> findActiveByCustomerId(String id) {
            return storedCarts.findActiveByCustomerId(id);
        }
        @Override public void save(Cart cart) {
            if (pauseNextSave.getAndSet(false)) {
                firstSaving.countDown();
                await(finishSave);
                if (failFirstSave) throw saveFailure;
            }
            storedCarts.save(cart);
        }
    };

    @BeforeEach void setup() {
        customers.save(customer);
        when(user.isAuthenticated()).thenReturn(true);
        when(user.getUserId()).thenReturn("u");
        when(screenings.findById("s")).thenReturn(Optional.of(
                new Screening("s", "movie", "hall", now, now.plusSeconds(3600), price)));
        when(seats.findById("a1")).thenReturn(Optional.of(new Seat("a1", "hall", "A", 1, SeatType.NORMAL)));
        when(seats.findById("a2")).thenReturn(Optional.of(new Seat("a2", "hall", "A", 2, SeatType.NORMAL)));
    }

    private Cart seedCart() {
        var cart = new Cart("cart", "c", List.of(new CartItem("s", "a1", price)));
        storedCarts.save(cart);
        assertTrue(holds.hold("c", "s", "a1"));
        return cart;
    }

    private Void add(String seat) {
        return new AddSeatToCartCommandHandler(user, customers, screenings, seats, reservations, holds, carts,
                () -> "cart-" + cartIds.incrementAndGet()).handle(new AddSeatToCartCommand("s", seat));
    }

    private Void mutate(Mutation mutation) {
        return switch (mutation) {
            case ADD -> add("a2");
            case REMOVE -> new RemoveSeatFromCartCommandHandler(user, customers, carts, holds)
                    .handle(new RemoveSeatFromCartCommand("s", "a1"));
            case CLEAR -> new ClearCartCommandHandler(user, customers, carts, holds).handle(new ClearCartCommand());
        };
    }

    private CheckoutResult checkout() {
        return new CheckoutCartCommandHandler(user, customers, carts, bookings, holds,
                () -> UUID.randomUUID().toString(), () -> now).handle(new CheckoutCartCommand());
    }

    private static Object outcome(Callable<?> action) throws Exception {
        try { return action.call(); }
        catch (RuntimeException ex) { return ex; }
    }

    private static void await(CountDownLatch latch) {
        try { assertTrue(latch.await(10, TimeUnit.SECONDS), "Concurrent operation did not reach its barrier"); }
        catch (InterruptedException ex) { Thread.currentThread().interrupt(); throw new AssertionError(ex); }
    }

    // Observe an actual monitor wait, not elapsed time, before releasing the first operation.
    private static void awaitBlockedOn(Thread thread, Object monitor, Future<?> task) {
        var threads = ManagementFactory.getThreadMXBean();
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (System.nanoTime() < deadline) {
            var info = threads.getThreadInfo(thread.threadId());
            if (info != null && info.getThreadState() == Thread.State.BLOCKED && info.getLockInfo() != null
                    && info.getLockInfo().getIdentityHashCode() == System.identityHashCode(monitor)) return;
            assertFalse(task.isDone(), "Competing operation completed before the first mutation/rollback finished");
            Thread.yield();
        }
        fail("Competing operation never waited on the shared monitor");
    }

    private Object[] overlap(Callable<?> first, Callable<?> second, Object monitor) throws Exception {
        try (var executor = Executors.newFixedThreadPool(2)) {
            var firstResult = executor.submit(() -> outcome(first));
            var contender = new AtomicReference<Thread>();
            var started = new CountDownLatch(1);
            try {
                await(firstSaving);
                var secondResult = executor.submit(() -> {
                    contender.set(Thread.currentThread());
                    started.countDown();
                    return outcome(second);
                });
                await(started);
                awaitBlockedOn(contender.get(), monitor, secondResult);
                finishSave.countDown();
                return new Object[] {firstResult.get(10, TimeUnit.SECONDS), secondResult.get(10, TimeUnit.SECONDS)};
            } finally {
                finishSave.countDown();
            }
        }
    }

    private Set<String> bookedSeats() {
        var result = new HashSet<String>();
        for (var booking : bookings.findByCustomerId("c")) {
            booking.getItems().forEach(item -> assertTrue(result.add(item.seatId()), "Duplicate booked item"));
        }
        return result;
    }

    @ParameterizedTest @EnumSource(Mutation.class)
    void checkoutWaitsForCartMutation(Mutation mutation) throws Exception {
        var cart = seedCart();
        var results = overlap(() -> mutate(mutation), this::checkout, cart);
        assertNull(results[0]);
        if (mutation == Mutation.ADD) {
            assertInstanceOf(CheckoutResult.class, results[1]);
            assertEquals(Set.of("a1", "a2"), bookedSeats());
            assertEquals(1, bookings.findByCustomerId("c").size());
            assertTrue(holds.isHeldBy("c", "s", "a1"));
            assertTrue(holds.isHeldBy("c", "s", "a2"));
        } else {
            assertInstanceOf(ValidationException.class, results[1]);
            assertTrue(bookings.findByCustomerId("c").isEmpty());
            assertFalse(holds.isHeld("s", "a1"));
        }
        assertTrue(cart.getItems().isEmpty());
    }

    @ParameterizedTest @EnumSource(Mutation.class)
    void cartMutationWaitsForCheckout(Mutation mutation) throws Exception {
        var cart = seedCart();
        var results = overlap(this::checkout, () -> mutate(mutation), cart);
        assertInstanceOf(CheckoutResult.class, results[0]);
        assertEquals(Set.of("a1"), bookedSeats());
        assertEquals(1, bookings.findByCustomerId("c").size());
        assertTrue(holds.isHeldBy("c", "s", "a1"));
        if (mutation == Mutation.ADD) {
            assertNull(results[1]);
            assertEquals(List.of(new CartItem("s", "a2", price)), cart.getItems());
            assertTrue(holds.isHeldBy("c", "s", "a2"));
        } else {
            if (mutation == Mutation.REMOVE) assertInstanceOf(NotFoundException.class, results[1]);
            else assertNull(results[1]);
            assertTrue(cart.getItems().isEmpty());
        }
    }

    @ParameterizedTest @EnumSource(Mutation.class)
    void checkoutWaitsForFailedMutationToRestoreItemsAndHolds(Mutation mutation) throws Exception {
        var cart = seedCart();
        failFirstSave = true;
        var results = overlap(() -> mutate(mutation), this::checkout, cart);
        assertSame(saveFailure, results[0]);
        assertInstanceOf(CheckoutResult.class, results[1]);
        assertEquals(Set.of("a1"), bookedSeats());
        assertEquals(1, bookings.findByCustomerId("c").size());
        assertTrue(cart.getItems().isEmpty());
        assertTrue(holds.isHeldBy("c", "s", "a1"));
        assertFalse(holds.isHeld("s", "a2"));
    }

    @ParameterizedTest @ValueSource(booleans = {false, true})
    void concurrentFirstAddsUseOneCartWithoutLostItemsOrHolds(boolean sameSeat) throws Exception {
        var results = overlap(() -> add("a1"), () -> add(sameSeat ? "a1" : "a2"), customer);
        assertNull(results[0]);
        var cart = storedCarts.findActiveByCustomerId("c").orElseThrow();
        assertEquals(1, cartIds.get());
        if (sameSeat) {
            assertInstanceOf(ConflictException.class, results[1]);
            assertEquals(List.of(new CartItem("s", "a1", price)), cart.getItems());
        } else {
            assertNull(results[1]);
            assertEquals(List.of(new CartItem("s", "a1", price), new CartItem("s", "a2", price)), cart.getItems());
            assertTrue(holds.isHeldBy("c", "s", "a2"));
        }
        assertTrue(holds.isHeldBy("c", "s", "a1"));
        assertTrue(bookings.findByCustomerId("c").isEmpty());
    }

    @ParameterizedTest @ValueSource(booleans = {false, true})
    void failedFirstCartCreationRollsBackBeforeCompetingAdd(boolean sameSeat) throws Exception {
        failFirstSave = true;
        String secondSeat = sameSeat ? "a1" : "a2";
        var results = overlap(() -> add("a1"), () -> add(secondSeat), customer);
        assertSame(saveFailure, results[0]);
        assertNull(results[1]);
        assertEquals(List.of(new CartItem("s", secondSeat, price)), storedCarts.findActiveByCustomerId("c").orElseThrow().getItems());
        assertEquals(2, cartIds.get());
        assertTrue(holds.isHeldBy("c", "s", secondSeat));
        assertEquals(sameSeat, holds.isHeld("s", "a1"));
        assertTrue(bookings.findByCustomerId("c").isEmpty());
    }
}

package cinema.application.bookings.commands;

import cinema.application.common.exceptions.*;
import cinema.application.common.interfaces.*;
import cinema.domain.booking.*;
import cinema.domain.cart.*;
import cinema.domain.common.DomainException;
import cinema.domain.common.Money;
import cinema.domain.customer.Customer;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.function.Supplier;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import cinema.infrastructure.persistence.inmemory.InMemoryCartRepository;
import cinema.infrastructure.persistence.inmemory.InMemoryBookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CheckoutCartCommandHandlerTest {
    private final CurrentUser user = mock(CurrentUser.class);
    private final CustomerRepository customers = mock(CustomerRepository.class);
    private final CartRepository carts = mock(CartRepository.class);
    private final BookingRepository bookings = mock(BookingRepository.class);
    private final SeatHoldService holds = mock(SeatHoldService.class);
    @SuppressWarnings("unchecked")
    private final Supplier<String> ids = mock(Supplier.class);
    private final Clock clock = mock(Clock.class);
    private final Instant now = Instant.parse("2026-09-28T00:00:00Z");
    private final CheckoutCartCommand command = new CheckoutCartCommand();
    private final CheckoutCartCommandHandler handler = new CheckoutCartCommandHandler(user, customers, carts, bookings, holds, ids, clock);

    @BeforeEach
    void setup() {
        when(user.isAuthenticated()).thenReturn(true);
        when(user.getUserId()).thenReturn("u1");
        when(customers.findByUserId("u1")).thenReturn(Optional.of(new Customer("c1", "u1", "Customer")));
        when(holds.isHeldBy(anyString(), anyString(), anyString())).thenReturn(true);
        when(ids.get()).thenReturn("b1", "b2", "b3");
        when(clock.now()).thenReturn(now);
    }
    private CartItem item(String screening, String seat, String amount, String currency) {
        return new CartItem(screening, seat, new Money(new BigDecimal(amount), Currency.getInstance(currency)));
    }
    private Cart cart(CartItem... items) {
        Cart cart = spy(new Cart("cart-1", "c1", List.of(items)));
        when(carts.findActiveByCustomerId("c1")).thenReturn(Optional.of(cart));
        return cart;
    }
    @Test
    void nullCommand() {
        assertThrows(ValidationException.class, () -> handler.handle(null));
        verifyNoInteractions(user, customers, carts, bookings, holds, ids, clock);
    }
    @Test
    void unauthenticated() {
        when(user.isAuthenticated()).thenReturn(false);
        assertThrows(ForbiddenException.class, () -> handler.handle(command));
        verify(user, never()).getUserId();
        verifyNoInteractions(customers, carts, bookings, holds, ids, clock);
    }
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t\n"})
    void invalidUserId(String invalid) {
        when(user.getUserId()).thenReturn(invalid);
        assertThrows(ForbiddenException.class, () -> handler.handle(command));
        verifyNoInteractions(customers, carts, bookings, holds, ids, clock);
    }
    @Test
    void missingCustomer() {
        when(customers.findByUserId("u1")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> handler.handle(command));
        verifyNoInteractions(carts, bookings, holds, ids, clock);
    }
    @Test
    void missingCart() {
        assertThrows(NotFoundException.class, () -> handler.handle(command));
        verifyNoInteractions(bookings, holds, ids, clock);
        verify(carts, never()).save(any());
    }
    @Test
    void emptyCart() {
        Cart cart = cart();
        assertThrows(ValidationException.class, () -> handler.handle(command));
        verifyNoInteractions(bookings, holds, ids, clock);
        verify(cart, never()).clear();
        verify(carts, never()).save(any());
    }
    @Test
    void lostHoldSavesNothingAndKeepsCart() {
        Cart cart = cart(item("s1", "a1", "10", "USD"), item("s2", "a2", "20", "USD"));
        var before = cart.getItems();
        when(holds.isHeldBy("c1", "s2", "a2")).thenReturn(false);
        assertThrows(ConflictException.class, () -> handler.handle(command));
        assertEquals(before, cart.getItems());
        verifyNoInteractions(bookings, ids, clock);
        verify(cart, never()).clear();
        verify(carts, never()).save(any());
    }
    @Test
    void oneItemCreatesPendingBookingWithSnapshotAndClockTime() {
        CartItem item = item("s1", "a1", "12.50", "USD");
        Cart cart = cart(item);
        assertEquals(List.of("b1"), handler.handle(command).bookingIds());
        var capture = ArgumentCaptor.forClass(Booking.class);
        verify(bookings).save(capture.capture());
        Booking saved = capture.getValue();
        assertEquals("b1", saved.getId());
        assertEquals("c1", saved.getCustomerId());
        assertEquals("s1", saved.getScreeningId());
        assertEquals(BookingStatus.PENDING, saved.getStatus());
        assertEquals(now, saved.getCreatedAt());
        assertEquals(item.unitPrice(), saved.getTotalAmount());
        assertEquals(List.of(new BookingItem("a1", item.unitPrice())), saved.getItems());
        assertTrue(cart.getItems().isEmpty());
        verify(clock).now();
        verify(carts).save(cart);
        verify(holds).isHeldBy("c1", "s1", "a1");
        verifyNoMoreInteractions(holds);
        verify(cart, never()).calculateTotal();
    }
    @Test
    void groupsInterleavedScreeningsInFirstOccurrenceOrderAndChecksAllHoldsFirst() {
        CartItem first = item("s2", "a2", "12.50", "USD");
        CartItem second = item("s1", "a1", "20", "EUR");
        CartItem third = item("s2", "a3", "0.125", "USD");
        Cart cart = cart(first, second, third);
        assertEquals(List.of("b1", "b2"), handler.handle(command).bookingIds());
        var capture = ArgumentCaptor.forClass(Booking.class);
        verify(bookings, times(2)).save(capture.capture());
        var saved = capture.getAllValues();
        assertEquals(List.of("s2", "s1"), saved.stream().map(Booking::getScreeningId).toList());
        assertEquals(List.of(new BookingItem("a2", first.unitPrice()), new BookingItem("a3", third.unitPrice())), saved.get(0).getItems());
        assertEquals(new BigDecimal("12.625"), saved.get(0).getTotalAmount().amount());
        assertEquals(Currency.getInstance("USD"), saved.get(0).getTotalAmount().currency());
        assertEquals(second.unitPrice(), saved.get(1).getTotalAmount());
        saved.forEach(b -> { assertEquals(BookingStatus.PENDING, b.getStatus()); assertEquals(now, b.getCreatedAt()); });
        var order = inOrder(holds, bookings, cart, carts);
        order.verify(holds).isHeldBy("c1", "s2", "a2");
        order.verify(holds).isHeldBy("c1", "s1", "a1");
        order.verify(holds).isHeldBy("c1", "s2", "a3");
        order.verify(bookings).save(saved.get(0));
        order.verify(bookings).save(saved.get(1));
        order.verify(cart).clear();
        order.verify(carts).save(cart);
        verifyNoMoreInteractions(holds);
        verify(cart, never()).calculateTotal();
    }
    @Test
    void laterGroupMixedCurrenciesFailsBeforeAnyPersistence() {
        Cart cart = cart(item("s1", "a1", "10", "USD"), item("s2", "a2", "20", "USD"), item("s2", "a3", "30", "EUR"));
        var before = cart.getItems();
        assertThrows(DomainException.class, () -> handler.handle(command));
        assertEquals(before, cart.getItems());
        verifyNoInteractions(bookings);
        verify(carts, never()).save(any());
        verify(cart, never()).clear();
        verify(holds, never()).release(anyString(), anyString(), anyString());
    }
    @Test
    void invalidLaterGeneratedIdFailsBeforeSavingFirstBooking() {
        Cart cart = cart(item("s1", "a1", "10", "USD"), item("s2", "a2", "20", "USD"));
        when(ids.get()).thenReturn("b1", " ");
        assertThrows(DomainException.class, () -> handler.handle(command));
        verifyNoInteractions(bookings);
        verify(cart, never()).clear();
        verify(carts, never()).save(any());
    }
    @Test
    void bookingSaveFailureDoesNotClearCartOrReleaseHolds() {
        Cart cart = cart(item("s1", "a1", "10", "USD"));
        RuntimeException original = new IllegalStateException("save failed");
        doThrow(original).when(bookings).save(any());
        assertSame(original, assertThrows(RuntimeException.class, () -> handler.handle(command)));
        verify(cart, never()).clear();
        verify(carts, never()).save(any());
        verify(holds, never()).release(anyString(), anyString(), anyString());
    }

    @Test
    void concurrentCheckoutConsumesSameCartExactlyOnceAcrossHandlerInstances() throws Exception {
        var realCarts = new InMemoryCartRepository();
        var concurrentReads = mock(CartRepository.class);
        var realBookings = new InMemoryBookingRepository();
        var cart = new Cart("cart", "c1", List.of(item("s1", "a1", "10", "USD"),
                item("s2", "a2", "20", "USD"), item("s1", "a3", "10", "USD")));
        realCarts.save(cart);
        var bothLoaded = new CountDownLatch(2);
        // Wait outside the real repository's monitor so both lookups can finish.
        doAnswer(invocation -> {
            var found = realCarts.findActiveByCustomerId("c1");
            bothLoaded.countDown();
            assertTrue(bothLoaded.await(5, TimeUnit.SECONDS));
            return found;
        }).when(concurrentReads).findActiveByCustomerId("c1");
        doAnswer(invocation -> {
            realCarts.save(invocation.getArgument(0));
            return null;
        }).when(concurrentReads).save(any());
        Supplier<String> uniqueIds = () -> UUID.randomUUID().toString();
        var firstHandler = new CheckoutCartCommandHandler(user, customers, concurrentReads, realBookings, holds, uniqueIds, clock);
        var secondHandler = new CheckoutCartCommandHandler(user, customers, concurrentReads, realBookings, holds, uniqueIds, clock);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> checkoutOutcome(firstHandler));
            var second = executor.submit(() -> checkoutOutcome(secondHandler));
            var results = List.of(first.get(5, TimeUnit.SECONDS), second.get(5, TimeUnit.SECONDS));
            assertEquals(1, results.stream().filter(cinema.application.common.models.CheckoutResult.class::isInstance).count());
            assertEquals(1, results.stream().filter(ValidationException.class::isInstance).count());
            var success = (cinema.application.common.models.CheckoutResult) results.stream()
                    .filter(cinema.application.common.models.CheckoutResult.class::isInstance).findFirst().orElseThrow();
            assertEquals(2, success.bookingIds().size());
            assertEquals(new HashSet<>(success.bookingIds()), new HashSet<>(realBookings.findByCustomerId("c1")
                    .stream().map(Booking::getId).toList()));
        }
        var saved = realBookings.findByCustomerId("c1");
        assertEquals(2, saved.size());
        assertEquals(Set.of("s1", "s2"), new HashSet<>(saved.stream().map(Booking::getScreeningId).toList()));
        assertEquals(3, saved.stream().mapToInt(b -> b.getItems().size()).sum());
        assertTrue(cart.getItems().isEmpty());
        verify(concurrentReads, times(1)).save(cart);
        verify(holds).isHeldBy("c1", "s1", "a1");
        verify(holds).isHeldBy("c1", "s2", "a2");
        verify(holds).isHeldBy("c1", "s1", "a3");
        verify(holds, never()).release(anyString(), anyString(), anyString());
    }

    private Object checkoutOutcome(CheckoutCartCommandHandler checkout) {
        try {
            return checkout.handle(command);
        } catch (ValidationException expected) {
            return expected;
        }
    }

    @Test
    void checkoutOfDifferentCartProceedsWhileFirstCartValidates() throws Exception {
        var realCarts = new InMemoryCartRepository();
        var realBookings = new InMemoryBookingRepository();
        realCarts.save(new Cart("cart1", "c1", List.of(item("s1", "a1", "10", "USD"))));
        realCarts.save(new Cart("cart2", "c2", List.of(item("s2", "a2", "20", "USD"))));
        var secondUser = mock(CurrentUser.class);
        when(secondUser.isAuthenticated()).thenReturn(true);
        when(secondUser.getUserId()).thenReturn("u2");
        when(customers.findByUserId("u2")).thenReturn(Optional.of(new Customer("c2", "u2", "Second")));
        var validatingFirst = new CountDownLatch(1);
        var secondFinished = new CountDownLatch(1);
        when(holds.isHeldBy("c1", "s1", "a1")).thenAnswer(invocation -> {
            validatingFirst.countDown();
            assertTrue(secondFinished.await(5, TimeUnit.SECONDS));
            return true;
        });
        Supplier<String> uniqueIds = () -> UUID.randomUUID().toString();
        var firstHandler = new CheckoutCartCommandHandler(user, customers, realCarts, realBookings, holds, uniqueIds, clock);
        var secondHandler = new CheckoutCartCommandHandler(secondUser, customers, realCarts, realBookings, holds, uniqueIds, clock);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> firstHandler.handle(command));
            var second = executor.submit(() -> {
                assertTrue(validatingFirst.await(5, TimeUnit.SECONDS));
                try {
                    return secondHandler.handle(command);
                } finally {
                    secondFinished.countDown();
                }
            });
            assertEquals(1, second.get(5, TimeUnit.SECONDS).bookingIds().size());
            assertEquals(1, first.get(5, TimeUnit.SECONDS).bookingIds().size());
        }
        assertEquals(1, realBookings.findByCustomerId("c1").size());
        assertEquals(1, realBookings.findByCustomerId("c2").size());
    }
}

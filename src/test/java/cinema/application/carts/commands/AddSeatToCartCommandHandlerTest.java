package cinema.application.carts.commands;

import cinema.application.common.exceptions.*;
import cinema.application.common.interfaces.*;
import cinema.domain.cart.Cart;
import cinema.domain.cart.CartItem;
import cinema.domain.cinema.Seat;
import cinema.domain.common.Money;
import cinema.domain.customer.Customer;
import cinema.domain.screening.Screening;
import cinema.domain.screening.ScreeningStatus;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import cinema.infrastructure.persistence.inmemory.InMemorySeatState;
import cinema.infrastructure.persistence.inmemory.InMemorySeatReservationRepository;
import cinema.infrastructure.persistence.inmemory.SimpleSeatHoldService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AddSeatToCartCommandHandlerTest {
    private final CurrentUser user = mock(CurrentUser.class);
    private final CustomerRepository customers = mock(CustomerRepository.class);
    private final ScreeningRepository screenings = mock(ScreeningRepository.class);
    private final SeatRepository seats = mock(SeatRepository.class);
    private final SeatReservationRepository reservations = mock(SeatReservationRepository.class);
    private final SeatHoldService holds = mock(SeatHoldService.class);
    private final CartRepository carts = mock(CartRepository.class);
    private final Supplier<String> ids = mock(Supplier.class);
    private final Screening screening = mock(Screening.class);
    private final Seat seat = mock(Seat.class);
    private final Money price = new Money(BigDecimal.TEN, Currency.getInstance("USD"));
    private final AddSeatToCartCommand command = new AddSeatToCartCommand("s1", "a1");
    private final AddSeatToCartCommandHandler handler = new AddSeatToCartCommandHandler(
            user, customers, screenings, seats, reservations, holds, carts, ids);

    @BeforeEach
    void setup() {
        when(user.isAuthenticated()).thenReturn(true);
        when(user.getUserId()).thenReturn("u1");
        when(customers.findByUserId("u1")).thenReturn(Optional.of(new Customer("c1", "u1", "Customer")));
        when(screenings.findById("s1")).thenReturn(Optional.of(screening));
        when(screening.getStatus()).thenReturn(ScreeningStatus.SCHEDULED);
        when(screening.getHallId()).thenReturn("h1");
        when(screening.getBasePrice()).thenReturn(price);
        when(seats.findById("a1")).thenReturn(Optional.of(seat));
        when(seat.getHallId()).thenReturn("h1");
        when(ids.get()).thenReturn("cart-1");
        when(holds.hold("c1", "s1", "a1")).thenReturn(true);
    }

    @Test
    void createsCartWithGeneratedIdAndScreeningPrice() {
        assertNull(handler.handle(command));
        var capture = ArgumentCaptor.forClass(Cart.class);
        verify(carts).save(capture.capture());
        Cart cart = capture.getValue();
        assertEquals("cart-1", cart.getId());
        assertEquals("c1", cart.getCustomerId());
        assertEquals(List.of(new CartItem("s1", "a1", price)), cart.getItems());
        verify(ids).get();
        var order = inOrder(holds, carts);
        order.verify(carts).findActiveByCustomerId("c1");
        order.verify(holds).isHeldBy("c1", "s1", "a1");
        order.verify(holds).hold("c1", "s1", "a1");
        order.verify(carts).save(cart);
        verify(holds, never()).release(anyString(), anyString(), anyString());
    }

    private Cart existingCart() {
        Cart cart = new Cart("existing", "c1", List.of(new CartItem("s2", "a1", price)));
        when(carts.findActiveByCustomerId("c1")).thenReturn(Optional.of(cart));
        return cart;
    }

    @Test
    void reusesExistingCart() {
        Cart cart = existingCart();
        handler.handle(command);
        verify(carts).save(same(cart));
        verifyNoInteractions(ids);
        assertEquals(List.of(new CartItem("s2", "a1", price), new CartItem("s1", "a1", price)), cart.getItems());
    }

    @Test
    void sameOwnerPreExistingHoldWorks() {
        when(holds.isHeldBy("c1", "s1", "a1")).thenReturn(true);
        handler.handle(command);
        verify(carts).save(any(Cart.class));
        verify(holds).hold("c1", "s1", "a1");
        verify(holds, never()).release(anyString(), anyString(), anyString());
    }

    @Test
    void rejectsNullCommandBeforeAnyDependency() {
        assertThrows(ValidationException.class, () -> handler.handle(null));
        verifyNoInteractions(user, customers, screenings, seats, reservations, carts, holds, ids);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t\n"})
    void rejectsInvalidCommandIds(String invalid) {
        assertThrows(ValidationException.class, () -> handler.handle(new AddSeatToCartCommand(invalid, "a1")));
        assertThrows(ValidationException.class, () -> handler.handle(new AddSeatToCartCommand("s1", invalid)));
        verifyNoInteractions(user, customers, screenings, seats, reservations, carts, holds, ids);
    }

    @Test
    void requiresAuthentication() {
        when(user.isAuthenticated()).thenReturn(false);
        assertThrows(ForbiddenException.class, () -> handler.handle(command));
        verify(user, never()).getUserId();
        verifyNoInteractions(customers, screenings, seats, reservations, carts, holds, ids);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t\n"})
    void requiresNonblankAuthenticatedUserId(String invalid) {
        when(user.getUserId()).thenReturn(invalid);
        assertThrows(ForbiddenException.class, () -> handler.handle(command));
        verifyNoInteractions(customers, screenings, seats, reservations, carts, holds, ids);
    }

    @Test
    void missingCustomer() {
        when(customers.findByUserId("u1")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> handler.handle(command));
        verifyNoInteractions(screenings, seats, reservations, carts, holds, ids);
    }

    @Test
    void missingScreening() {
        when(screenings.findById("s1")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> handler.handle(command));
        verifyNoInteractions(seats, reservations, carts, holds, ids);
    }

    @ParameterizedTest
    @EnumSource(value = ScreeningStatus.class, names = {"CANCELLED", "COMPLETED"})
    void rejectsNonScheduledScreening(ScreeningStatus status) {
        when(screening.getStatus()).thenReturn(status);
        assertThrows(ValidationException.class, () -> handler.handle(command));
        verifyNoInteractions(seats, reservations, carts, holds, ids);
    }

    @Test
    void rejectsNullScreeningStatus() {
        when(screening.getStatus()).thenReturn(null);
        assertThrows(ValidationException.class, () -> handler.handle(command));
        verifyNoInteractions(seats, reservations, carts, holds, ids);
    }

    @Test
    void missingSeat() {
        when(seats.findById("a1")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> handler.handle(command));
        verifyNoInteractions(reservations, carts, holds, ids);
    }

    @Test
    void seatInWrongHall() {
        when(seat.getHallId()).thenReturn("other-hall");
        assertThrows(ValidationException.class, () -> handler.handle(command));
        verifyNoInteractions(reservations, carts, holds, ids);
    }

    @Test
    void bookedSeatConflicts() {
        when(reservations.isBooked("s1", "a1")).thenReturn(true);
        assertThrows(ConflictException.class, () -> handler.handle(command));
        verifyNoInteractions(carts, holds, ids);
    }

    @Test
    void duplicateRejectedBeforeHold() {
        Cart cart = new Cart("existing", "c1", List.of(new CartItem("s1", "a1", price)));
        when(carts.findActiveByCustomerId("c1")).thenReturn(Optional.of(cart));
        assertThrows(ConflictException.class, () -> handler.handle(command));
        verifyNoInteractions(holds, ids);
        verify(carts, never()).save(any());
        assertEquals(List.of(new CartItem("s1", "a1", price)), cart.getItems());
    }

    @Test
    void anotherOwnerHoldConflictsWithoutMutation() {
        Cart cart = existingCart();
        var before = cart.getItems();
        when(holds.hold("c1", "s1", "a1")).thenReturn(false);
        assertThrows(ConflictException.class, () -> handler.handle(command));
        assertEquals(before, cart.getItems());
        verify(carts, never()).save(any());
        verify(holds, never()).release(anyString(), anyString(), anyString());
    }

    @Test
    void saveFailureRollsBackNewItemAndNewHold() {
        Cart cart = existingCart();
        var before = cart.getItems();
        RuntimeException failure = new IllegalStateException("save failed");
        doThrow(failure).when(carts).save(cart);
        assertSame(failure, assertThrows(RuntimeException.class, () -> handler.handle(command)));
        assertEquals(before, cart.getItems());
        verify(holds).release("c1", "s1", "a1");
    }

    @Test
    void saveFailurePreservesPreExistingHold() {
        Cart cart = existingCart();
        var before = cart.getItems();
        when(holds.isHeldBy("c1", "s1", "a1")).thenReturn(true);
        RuntimeException failure = new IllegalStateException("save failed");
        doThrow(failure).when(carts).save(cart);
        assertSame(failure, assertThrows(RuntimeException.class, () -> handler.handle(command)));
        assertEquals(before, cart.getItems());
        verify(holds, never()).release(anyString(), anyString(), anyString());
    }

    @Test
    void newCartIsEmptyAfterSaveFailure() {
        RuntimeException failure = new IllegalStateException("save failed");
        var capture = ArgumentCaptor.forClass(Cart.class);
        doThrow(failure).when(carts).save(any());
        assertSame(failure, assertThrows(RuntimeException.class, () -> handler.handle(command)));
        verify(carts).save(capture.capture());
        assertTrue(capture.getValue().getItems().isEmpty());
        verify(holds).release("c1", "s1", "a1");
    }

    @Test
    void failureBeforeAddCompletesReleasesNewHold() {
        Cart cart = existingCart();
        var before = cart.getItems();
        RuntimeException failure = new IllegalStateException("price read failed");
        when(screening.getBasePrice()).thenThrow(failure);
        assertSame(failure, assertThrows(RuntimeException.class, () -> handler.handle(command)));
        assertEquals(before, cart.getItems());
        verify(carts, never()).save(any());
        verify(holds).release("c1", "s1", "a1");
    }

    @Test
    void cleanupFailureDoesNotReplaceOriginalFailure() {
        Cart cart = existingCart();
        var before = cart.getItems();
        RuntimeException failure = new IllegalStateException("save failed");
        RuntimeException cleanup = new IllegalStateException("release failed");
        doThrow(failure).when(carts).save(cart);
        doThrow(cleanup).when(holds).release("c1", "s1", "a1");
        assertSame(failure, assertThrows(RuntimeException.class, () -> handler.handle(command)));
        assertEquals(before, cart.getItems());
        assertArrayEquals(new Throwable[] {cleanup}, failure.getSuppressed());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void bookingBetweenPrecheckAndHoldRejectsAddWithoutChangingCart(boolean existing) throws Exception {
        Cart cart = existing ? existingCart() : null;
        var before = existing ? cart.getItems() : List.<CartItem>of();
        var state = new InMemorySeatState();
        var realReservations = spy(new InMemorySeatReservationRepository(state));
        var realHolds = new SimpleSeatHoldService(state);
        var checked = new CountDownLatch(1);
        var booked = new CountDownLatch(1);
        doAnswer(invocation -> {
            boolean result = (boolean) invocation.callRealMethod();
            checked.countDown();
            assertTrue(booked.await(5, TimeUnit.SECONDS));
            return result;
        }).when(realReservations).isBooked("s1", "a1");
        var racingHandler = new AddSeatToCartCommandHandler(user, customers, screenings, seats,
                realReservations, realHolds, carts, ids);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var add = executor.submit(() -> assertThrows(ConflictException.class, () -> racingHandler.handle(command)));
            var payment = executor.submit(() -> {
                assertTrue(checked.await(5, TimeUnit.SECONDS));
                try {
                    realReservations.markBooked("s1", "a1", "paid-booking");
                } finally {
                    booked.countDown();
                }
                return null;
            });
            payment.get(5, TimeUnit.SECONDS);
            add.get(5, TimeUnit.SECONDS);
        }
        assertFalse(realHolds.isHeld("s1", "a1"));
        assertTrue(realReservations.isBooked("s1", "a1"));
        if (existing) assertEquals(before, cart.getItems());
        verify(carts, never()).save(any());
    }
}

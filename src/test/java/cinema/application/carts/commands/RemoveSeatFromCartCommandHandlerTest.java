package cinema.application.carts.commands;

import cinema.application.common.exceptions.ForbiddenException;
import cinema.application.common.exceptions.NotFoundException;
import cinema.application.common.exceptions.ValidationException;
import cinema.application.common.interfaces.*;
import cinema.domain.cart.Cart;
import cinema.domain.cart.CartItem;
import cinema.domain.common.Money;
import cinema.domain.customer.Customer;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RemoveSeatFromCartCommandHandlerTest {
    private final CurrentUser user = mock(CurrentUser.class);
    private final CustomerRepository customers = mock(CustomerRepository.class);
    private final CartRepository carts = mock(CartRepository.class);
    private final SeatHoldService holds = mock(SeatHoldService.class);
    private final Money price = new Money(BigDecimal.TEN, Currency.getInstance("USD"));
    private final CartItem target = new CartItem("s1", "a1", price);
    private final CartItem otherScreening = new CartItem("s2", "a1", price);
    private final CartItem otherSeat = new CartItem("s1", "a2", price);
    private final Cart cart = spy(new Cart("cart-1", "c1", List.of(otherScreening, otherSeat, target)));
    private final RemoveSeatFromCartCommand command = new RemoveSeatFromCartCommand("s1", "a1");
    private final RemoveSeatFromCartCommandHandler handler =
            new RemoveSeatFromCartCommandHandler(user, customers, carts, holds);

    @BeforeEach
    void setup() {
        when(user.isAuthenticated()).thenReturn(true);
        when(user.getUserId()).thenReturn("u1");
        when(customers.findByUserId("u1")).thenReturn(Optional.of(new Customer("c1", "u1", "Customer")));
        when(carts.findActiveByCustomerId("c1")).thenReturn(Optional.of(cart));
        when(holds.hold("c1", "s1", "a1")).thenReturn(true);
    }

    @Test
    void removesExactPairThenReleasesThenSaves() {
        assertNull(handler.handle(command));
        var order = inOrder(user, customers, carts, cart, holds);
        order.verify(user).isAuthenticated();
        order.verify(user).getUserId();
        order.verify(customers).findByUserId("u1");
        order.verify(carts).findActiveByCustomerId("c1");
        order.verify(cart).getItems();
        order.verify(cart).removeItem("s1", "a1");
        order.verify(holds).release("c1", "s1", "a1");
        order.verify(carts).save(cart);
        assertEquals(List.of(otherScreening, otherSeat), cart.getItems());
        verify(holds, never()).hold(anyString(), anyString(), anyString());
        verify(holds, never()).isHeldBy(anyString(), anyString(), anyString());
    }

    @Test
    void nullCommandRejectedBeforeDependencies() {
        assertThrows(ValidationException.class, () -> handler.handle(null));
        verifyNoInteractions(user, customers, carts, holds, cart);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t\n"})
    void invalidIdsRejectedBeforeDependencies(String invalid) {
        assertThrows(ValidationException.class, () -> handler.handle(new RemoveSeatFromCartCommand(invalid, "a1")));
        assertThrows(ValidationException.class, () -> handler.handle(new RemoveSeatFromCartCommand("s1", invalid)));
        verifyNoInteractions(user, customers, carts, holds, cart);
    }

    @Test
    void unauthenticatedRejected() {
        when(user.isAuthenticated()).thenReturn(false);
        assertThrows(ForbiddenException.class, () -> handler.handle(command));
        verify(user, never()).getUserId();
        verifyNoInteractions(customers, carts, holds, cart);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t\n"})
    void invalidAuthenticatedUserIdRejected(String invalid) {
        when(user.getUserId()).thenReturn(invalid);
        assertThrows(ForbiddenException.class, () -> handler.handle(command));
        verifyNoInteractions(customers, carts, holds, cart);
    }

    @Test
    void missingCustomerRejected() {
        when(customers.findByUserId("u1")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> handler.handle(command));
        verifyNoInteractions(carts, holds, cart);
    }

    @Test
    void missingCartRejected() {
        when(carts.findActiveByCustomerId("c1")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> handler.handle(command));
        verifyNoInteractions(holds, cart);
        verify(carts, never()).save(any());
    }

    @Test
    void missingExactPairRejectedWithoutMutation() {
        Cart missingPair = spy(new Cart("cart-2", "c1", List.of(otherScreening, otherSeat)));
        when(carts.findActiveByCustomerId("c1")).thenReturn(Optional.of(missingPair));
        assertThrows(NotFoundException.class, () -> handler.handle(command));
        verify(missingPair, never()).removeItem(anyString(), anyString());
        verifyNoInteractions(holds);
        verify(carts, never()).save(any());
        assertEquals(List.of(otherScreening, otherSeat), missingPair.getItems());
    }

    private void assertRestored() {
        assertEquals(List.of(otherScreening, otherSeat, target), cart.getItems());
        assertSame(target, cart.getItems().get(2));
    }

    @Test
    void saveFailureRestoresOriginalItemAndHold() {
        RuntimeException original = new IllegalStateException("save failed");
        doThrow(original).when(carts).save(cart);
        assertSame(original, assertThrows(RuntimeException.class, () -> handler.handle(command)));
        assertRestored();
        var order = inOrder(cart, holds, carts);
        order.verify(cart).removeItem("s1", "a1");
        order.verify(holds).release("c1", "s1", "a1");
        order.verify(carts).save(cart);
        order.verify(cart).addItem(same(target));
        order.verify(holds).hold("c1", "s1", "a1");
        verify(carts, times(1)).save(cart);
    }

    @Test
    void releaseFailureRestoresItemWithoutReacquiringHold() {
        RuntimeException original = new IllegalStateException("release failed");
        doThrow(original).when(holds).release("c1", "s1", "a1");
        assertSame(original, assertThrows(RuntimeException.class, () -> handler.handle(command)));
        assertRestored();
        verify(carts, never()).save(any());
        verify(holds, never()).hold(anyString(), anyString(), anyString());
    }

    @Test
    void saveErrorAlsoCompensates() {
        Error original = new AssertionError("save error");
        doThrow(original).when(carts).save(cart);
        assertSame(original, assertThrows(Error.class, () -> handler.handle(command)));
        assertRestored();
        verify(holds).hold("c1", "s1", "a1");
    }

    @Test
    void releaseErrorAlsoRestoresItem() {
        Error original = new AssertionError("release error");
        doThrow(original).when(holds).release("c1", "s1", "a1");
        assertSame(original, assertThrows(Error.class, () -> handler.handle(command)));
        assertRestored();
        verify(carts, never()).save(any());
        verify(holds, never()).hold(anyString(), anyString(), anyString());
    }

    @Test
    void cleanupFailuresAreSuppressedAndBothCleanupsAttempted() {
        RuntimeException original = new IllegalStateException("save failed");
        RuntimeException itemFailure = new IllegalStateException("restore item failed");
        Error holdFailure = new AssertionError("restore hold failed");
        doThrow(original).when(carts).save(cart);
        doThrow(itemFailure).when(cart).addItem(target);
        when(holds.hold("c1", "s1", "a1")).thenThrow(holdFailure);
        assertSame(original, assertThrows(RuntimeException.class, () -> handler.handle(command)));
        assertArrayEquals(new Throwable[] {itemFailure, holdFailure}, original.getSuppressed());
        verify(holds).hold("c1", "s1", "a1");
    }

    @Test
    void rejectedHoldRestorationIsSuppressed() {
        RuntimeException original = new IllegalStateException("save failed");
        doThrow(original).when(carts).save(cart);
        when(holds.hold("c1", "s1", "a1")).thenReturn(false);
        assertSame(original, assertThrows(RuntimeException.class, () -> handler.handle(command)));
        assertRestored();
        assertEquals(1, original.getSuppressed().length);
        assertInstanceOf(IllegalStateException.class, original.getSuppressed()[0]);
    }

    @Test
    void releaseFailureWithItemCleanupErrorPreservesOriginal() {
        RuntimeException original = new IllegalStateException("release failed");
        Error cleanup = new AssertionError("restore failed");
        doThrow(original).when(holds).release("c1", "s1", "a1");
        doThrow(cleanup).when(cart).addItem(target);
        assertSame(original, assertThrows(RuntimeException.class, () -> handler.handle(command)));
        assertArrayEquals(new Throwable[] {cleanup}, original.getSuppressed());
        verify(holds, never()).hold(anyString(), anyString(), anyString());
    }

    @Test
    void sameExceptionDuringCleanupDoesNotMaskOriginal() {
        RuntimeException original = new IllegalStateException("shared failure");
        doThrow(original).when(carts).save(cart);
        doThrow(original).when(cart).addItem(target);
        assertSame(original, assertThrows(RuntimeException.class, () -> handler.handle(command)));
        assertEquals(0, original.getSuppressed().length);
        verify(holds).hold("c1", "s1", "a1");
    }
}

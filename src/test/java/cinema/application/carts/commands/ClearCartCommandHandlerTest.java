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

class ClearCartCommandHandlerTest {
    private final CurrentUser user = mock(CurrentUser.class);
    private final CustomerRepository customers = mock(CustomerRepository.class);
    private final CartRepository carts = mock(CartRepository.class);
    private final SeatHoldService holds = mock(SeatHoldService.class);
    private final Money price = new Money(BigDecimal.TEN, Currency.getInstance("USD"));
    private final CartItem first = new CartItem("s1", "a1", price);
    private final CartItem second = new CartItem("s2", "a1", price);
    private final CartItem third = new CartItem("s2", "a2", price);
    private final List<CartItem> originalItems = List.of(first, second, third);
    private final Cart cart = spy(new Cart("cart-1", "c1", originalItems));
    private final ClearCartCommand command = new ClearCartCommand();
    private final ClearCartCommandHandler handler = new ClearCartCommandHandler(user, customers, carts, holds);

    @BeforeEach
    void setup() {
        when(user.isAuthenticated()).thenReturn(true);
        when(user.getUserId()).thenReturn("u1");
        when(customers.findByUserId("u1")).thenReturn(Optional.of(new Customer("c1", "u1", "Customer")));
        when(carts.findActiveByCustomerId("c1")).thenReturn(Optional.of(cart));
        when(holds.hold(anyString(), anyString(), anyString())).thenReturn(true);
    }

    @Test
    void clearsThenReleasesInSnapshotOrderThenSaves() {
        doAnswer(invocation -> {
            assertTrue(cart.getItems().isEmpty());
            return null;
        }).when(carts).save(cart);
        assertNull(handler.handle(command));
        var order = inOrder(user, customers, carts, cart, holds);
        order.verify(user).isAuthenticated();
        order.verify(user).getUserId();
        order.verify(customers).findByUserId("u1");
        order.verify(carts).findActiveByCustomerId("c1");
        order.verify(cart).getItems();
        order.verify(cart).clear();
        order.verify(holds).release("c1", "s1", "a1");
        order.verify(holds).release("c1", "s2", "a1");
        order.verify(holds).release("c1", "s2", "a2");
        order.verify(carts).save(cart);
        assertTrue(cart.getItems().isEmpty());
        verify(holds, never()).hold(anyString(), anyString(), anyString());
        verify(holds, never()).isHeldBy(anyString(), anyString(), anyString());
    }

    @Test
    void emptyCartIsSavedWithoutReleasing() {
        Cart empty = new Cart("empty", "c1", List.of());
        when(carts.findActiveByCustomerId("c1")).thenReturn(Optional.of(empty));
        assertNull(handler.handle(command));
        verify(carts).save(empty);
        verifyNoInteractions(holds);
        assertTrue(empty.getItems().isEmpty());
    }

    @Test
    void nullCommandRejectedBeforeDependencies() {
        assertThrows(ValidationException.class, () -> handler.handle(null));
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

    private void assertRestored() {
        assertEquals(originalItems, cart.getItems());
        for (int i = 0; i < originalItems.size(); i++) {
            assertSame(originalItems.get(i), cart.getItems().get(i));
        }
    }

    @Test
    void releaseFailureRestoresAllItemsAndOnlyCompletedReleases() {
        RuntimeException original = new IllegalStateException("second release failed");
        doThrow(original).when(holds).release("c1", "s2", "a1");
        assertSame(original, assertThrows(RuntimeException.class, () -> handler.handle(command)));
        assertRestored();
        verify(carts, never()).save(any());
        verify(holds, never()).release("c1", "s2", "a2");
        verify(holds, never()).hold("c1", "s2", "a1");
        verify(holds, never()).hold("c1", "s2", "a2");
        var order = inOrder(cart, holds);
        order.verify(holds).release("c1", "s1", "a1");
        order.verify(holds).release("c1", "s2", "a1");
        order.verify(cart).addItem(same(first));
        order.verify(cart).addItem(same(second));
        order.verify(cart).addItem(same(third));
        order.verify(holds).hold("c1", "s1", "a1");
    }

    @Test
    void firstReleaseFailureDoesNotReholdAnything() {
        RuntimeException original = new IllegalStateException("release failed");
        doThrow(original).when(holds).release("c1", "s1", "a1");
        assertSame(original, assertThrows(RuntimeException.class, () -> handler.handle(command)));
        assertRestored();
        verify(holds, never()).hold(anyString(), anyString(), anyString());
        verify(holds, never()).release("c1", "s2", "a1");
        verify(carts, never()).save(any());
    }

    @Test
    void saveFailureRestoresAllItemsAndHolds() {
        RuntimeException original = new IllegalStateException("save failed");
        doThrow(original).when(carts).save(cart);
        assertSame(original, assertThrows(RuntimeException.class, () -> handler.handle(command)));
        assertRestored();
        var order = inOrder(carts, cart, holds);
        order.verify(carts).save(cart);
        order.verify(cart).addItem(same(first));
        order.verify(cart).addItem(same(second));
        order.verify(cart).addItem(same(third));
        order.verify(holds).hold("c1", "s1", "a1");
        order.verify(holds).hold("c1", "s2", "a1");
        order.verify(holds).hold("c1", "s2", "a2");
        verify(carts, times(1)).save(cart);
    }

    @Test
    void rejectedRollbackHoldIsSuppressedAndLaterHoldsStillAttempted() {
        RuntimeException original = new IllegalStateException("save failed");
        doThrow(original).when(carts).save(cart);
        when(holds.hold("c1", "s1", "a1")).thenReturn(false);
        assertSame(original, assertThrows(RuntimeException.class, () -> handler.handle(command)));
        assertRestored();
        assertEquals(1, original.getSuppressed().length);
        assertInstanceOf(IllegalStateException.class, original.getSuppressed()[0]);
        verify(holds).hold("c1", "s2", "a1");
        verify(holds).hold("c1", "s2", "a2");
    }

    @Test
    void cleanupExceptionsDoNotStopRemainingCleanupOrReplaceOriginal() {
        RuntimeException original = new IllegalStateException("save failed");
        RuntimeException itemFailure = new IllegalStateException("item restore failed");
        Error holdFailure = new AssertionError("hold restore failed");
        doThrow(original).when(carts).save(cart);
        doThrow(itemFailure).when(cart).addItem(first);
        when(holds.hold("c1", "s1", "a1")).thenThrow(holdFailure);
        assertSame(original, assertThrows(RuntimeException.class, () -> handler.handle(command)));
        assertArrayEquals(new Throwable[] {itemFailure, holdFailure}, original.getSuppressed());
        assertEquals(List.of(second, third), cart.getItems());
        verify(holds).hold("c1", "s2", "a1");
        verify(holds).hold("c1", "s2", "a2");
    }

    @Test
    void releaseErrorAlsoCompensatesOnlyCompletedReleases() {
        Error original = new AssertionError("release error");
        doThrow(original).when(holds).release("c1", "s2", "a1");
        assertSame(original, assertThrows(Error.class, () -> handler.handle(command)));
        assertRestored();
        verify(holds).hold("c1", "s1", "a1");
        verify(holds, never()).hold("c1", "s2", "a1");
        verify(holds, never()).hold("c1", "s2", "a2");
        verify(carts, never()).save(any());
    }

    @Test
    void saveErrorAlsoRestoresItemsAndHolds() {
        Error original = new AssertionError("save error");
        doThrow(original).when(carts).save(cart);
        assertSame(original, assertThrows(Error.class, () -> handler.handle(command)));
        assertRestored();
        verify(holds).hold("c1", "s1", "a1");
        verify(holds).hold("c1", "s2", "a1");
        verify(holds).hold("c1", "s2", "a2");
    }

    @Test
    void sameCleanupExceptionDoesNotCauseSelfSuppression() {
        RuntimeException original = new IllegalStateException("shared failure");
        doThrow(original).when(carts).save(cart);
        doThrow(original).when(cart).addItem(first);
        assertSame(original, assertThrows(RuntimeException.class, () -> handler.handle(command)));
        assertEquals(0, original.getSuppressed().length);
        assertEquals(List.of(second, third), cart.getItems());
        verify(holds).hold("c1", "s1", "a1");
        verify(holds).hold("c1", "s2", "a1");
        verify(holds).hold("c1", "s2", "a2");
    }
}

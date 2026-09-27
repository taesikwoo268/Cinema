package cinema.application.carts.queries;

import cinema.application.common.exceptions.ForbiddenException;
import cinema.application.common.exceptions.NotFoundException;
import cinema.application.common.exceptions.ValidationException;
import cinema.application.common.interfaces.CartRepository;
import cinema.application.common.interfaces.CurrentUser;
import cinema.application.common.interfaces.CustomerRepository;
import cinema.application.common.models.CartItemView;
import cinema.application.common.models.CartView;
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

class GetMyCartQueryHandlerTest {
    private final CurrentUser user = mock(CurrentUser.class);
    private final CustomerRepository customers = mock(CustomerRepository.class);
    private final CartRepository carts = mock(CartRepository.class);
    private final GetMyCartQueryHandler handler = new GetMyCartQueryHandler(user, customers, carts);
    private final GetMyCartQuery query = new GetMyCartQuery();

    @BeforeEach
    void setup() {
        when(user.isAuthenticated()).thenReturn(true);
        when(user.getUserId()).thenReturn("user-1");
        when(customers.findByUserId("user-1"))
                .thenReturn(Optional.of(new Customer("customer-1", "user-1", "Customer")));
    }

    private void verifyReadOnly(Cart cart) {
        verify(customers, never()).save(any());
        verify(carts, never()).save(any());
        verify(cart, never()).addItem(any());
        verify(cart, never()).removeItem(anyString(), anyString());
        verify(cart, never()).clear();
        verify(cart, never()).calculateTotal();
    }

    @Test
    void mapsAllFieldsAndPreservesItemOrderWithoutMutation() {
        var first = new CartItem("screening-z", "seat-2",
                new Money(new BigDecimal("12.50"), Currency.getInstance("USD")));
        var second = new CartItem("screening-a", "seat-1",
                new Money(new BigDecimal("25000"), Currency.getInstance("VND")));
        var originalItems = List.of(first, second);
        Cart cart = spy(new Cart("cart-1", "customer-1", originalItems));
        when(carts.findActiveByCustomerId("customer-1")).thenReturn(Optional.of(cart));

        CartView view = handler.handle(query);

        assertEquals(new CartView("cart-1", "customer-1", List.of(
                new CartItemView("screening-z", "seat-2", new BigDecimal("12.50"), "USD"),
                new CartItemView("screening-a", "seat-1", new BigDecimal("25000"), "VND"))), view);
        assertEquals(originalItems, cart.getItems());
        verifyReadOnly(cart);
        var order = inOrder(user, customers, carts);
        order.verify(user).isAuthenticated();
        order.verify(user).getUserId();
        order.verify(customers).findByUserId("user-1");
        order.verify(carts).findActiveByCustomerId("customer-1");
        verifyNoMoreInteractions(customers, carts);
    }

    @Test
    void existingEmptyCartReturnsEmptyView() {
        Cart cart = spy(new Cart("empty-cart", "customer-1", List.of()));
        when(carts.findActiveByCustomerId("customer-1")).thenReturn(Optional.of(cart));
        assertEquals(new CartView("empty-cart", "customer-1", List.of()), handler.handle(query));
        assertTrue(cart.getItems().isEmpty());
        verifyReadOnly(cart);
    }

    @Test
    void nullQueryRejectedBeforeDependencies() {
        assertThrows(ValidationException.class, () -> handler.handle(null));
        verifyNoInteractions(user, customers, carts);
    }

    @Test
    void unauthenticatedRejectedBeforeUserIdOrRepositories() {
        when(user.isAuthenticated()).thenReturn(false);
        assertThrows(ForbiddenException.class, () -> handler.handle(query));
        verify(user, never()).getUserId();
        verifyNoInteractions(customers, carts);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t\n"})
    void invalidAuthenticatedUserIdRejected(String invalid) {
        when(user.getUserId()).thenReturn(invalid);
        assertThrows(ForbiddenException.class, () -> handler.handle(query));
        verifyNoInteractions(customers, carts);
    }

    @Test
    void missingCustomerRejectedBeforeCartLookup() {
        when(customers.findByUserId("user-1")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> handler.handle(query));
        verify(customers).findByUserId("user-1");
        verifyNoMoreInteractions(customers);
        verifyNoInteractions(carts);
    }

    @Test
    void missingCartRejectedWithoutSaving() {
        when(carts.findActiveByCustomerId("customer-1")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> handler.handle(query));
        verify(customers).findByUserId("user-1");
        verify(carts).findActiveByCustomerId("customer-1");
        verifyNoMoreInteractions(customers, carts);
    }
}

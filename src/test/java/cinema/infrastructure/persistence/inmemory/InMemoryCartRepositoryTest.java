package cinema.infrastructure.persistence.inmemory;

import cinema.domain.cart.Cart;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryCartRepositoryTest {
    private final InMemoryCartRepository repository = new InMemoryCartRepository();

    @Test
    void initiallyEmpty() {
        assertTrue(repository.findActiveByCustomerId("customer-1").isEmpty());
    }

    @Test
    void savesAndFindsCart() {
        Cart cart = new Cart("cart-1", "customer-1", List.of());
        repository.save(cart);
        Cart found = repository.findActiveByCustomerId("customer-1").orElseThrow();
        assertSame(cart, found);
        assertEquals("cart-1", found.getId());
        assertEquals("customer-1", found.getCustomerId());
    }

    @Test
    void differentCustomersAreIndependent() {
        Cart first = new Cart("cart-1", "customer-1", List.of());
        Cart second = new Cart("cart-2", "customer-2", List.of());
        repository.save(first);
        repository.save(second);
        assertSame(first, repository.findActiveByCustomerId("customer-1").orElseThrow());
        assertSame(second, repository.findActiveByCustomerId("customer-2").orElseThrow());
    }

    @Test
    void savingSameCustomerReplacesActiveCart() {
        Cart original = new Cart("cart-1", "customer-1", List.of());
        Cart replacement = new Cart("cart-2", "customer-1", List.of());
        Cart other = new Cart("cart-3", "customer-2", List.of());
        repository.save(original);
        repository.save(other);
        repository.save(replacement);
        assertSame(replacement, repository.findActiveByCustomerId("customer-1").orElseThrow());
        assertSame(other, repository.findActiveByCustomerId("customer-2").orElseThrow());
        assertDoesNotThrow(() -> repository.save(replacement));
        assertSame(replacement, repository.findActiveByCustomerId("customer-1").orElseThrow());
    }

    @Test
    void unknownCustomerReturnsEmpty() {
        repository.save(new Cart("cart-1", "customer-1", List.of()));
        assertTrue(repository.findActiveByCustomerId("unknown").isEmpty());
    }

    @Test
    void nullCartIsRejectedWithoutChangingStoredCart() {
        Cart cart = new Cart("cart-1", "customer-1", List.of());
        repository.save(cart);
        assertThrows(NullPointerException.class, () -> repository.save(null));
        assertSame(cart, repository.findActiveByCustomerId("customer-1").orElseThrow());
    }
}

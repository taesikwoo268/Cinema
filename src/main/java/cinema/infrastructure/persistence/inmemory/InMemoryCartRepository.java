package cinema.infrastructure.persistence.inmemory;

import cinema.application.common.interfaces.CartRepository;
import cinema.domain.cart.Cart;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** In-memory active carts; repository operations are synchronized, Cart mutations are not. */
public final class InMemoryCartRepository implements CartRepository {
    private final Map<String, Cart> cartsByCustomerId = new HashMap<>();

    @Override
    public synchronized Optional<Cart> findActiveByCustomerId(String customerId) {
        return Optional.ofNullable(cartsByCustomerId.get(customerId));
    }

    @Override
    public synchronized void save(Cart cart) {
        Objects.requireNonNull(cart, "Cart must not be null");
        cartsByCustomerId.put(cart.getCustomerId(), cart);
    }
}

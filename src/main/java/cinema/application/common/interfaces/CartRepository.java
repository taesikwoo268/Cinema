package cinema.application.common.interfaces;

import java.util.List;
import java.util.Optional;
import java.time.Instant;
import cinema.domain.cart.Cart;
public interface CartRepository {
    Optional<Cart> findActiveByCustomerId(String customerId);
    void save(Cart cart);
}

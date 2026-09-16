package cinema.infrastructure.persistence.adapters;

import java.util.List;
import java.util.Optional;
import java.time.Instant;
import cinema.domain.cart.Cart;
import cinema.application.common.interfaces.CartRepository;
/** Unwired until persistence mapping and transactional contracts are implemented. */
public final class JpaCartRepositoryAdapter implements CartRepository {
    @Override public Optional<Cart> findActiveByCustomerId(String customerId) { throw new UnsupportedOperationException("Architecture Contract v0.1: not implemented" ); }
    @Override public void save(Cart cart) { throw new UnsupportedOperationException("Architecture Contract v0.1: not implemented" ); }
}

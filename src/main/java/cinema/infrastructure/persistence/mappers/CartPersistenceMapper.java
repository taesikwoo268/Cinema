package cinema.infrastructure.persistence.mappers;

import cinema.domain.cart.Cart;
import cinema.infrastructure.persistence.entities.CartJpaEntity;
public interface CartPersistenceMapper {
    Cart toDomain(CartJpaEntity entity);
    CartJpaEntity toEntity(Cart model);
}

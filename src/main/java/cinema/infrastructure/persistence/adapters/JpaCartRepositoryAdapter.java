package cinema.infrastructure.persistence.adapters;

import cinema.application.common.interfaces.CartRepository;
import cinema.application.common.exceptions.ConflictException;
import cinema.domain.cart.Cart;
import cinema.infrastructure.persistence.entities.*;
import cinema.infrastructure.persistence.repositories.*;
import cinema.infrastructure.persistence.mappers.CartPersistenceMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Repository
@Profile("mysql")
@Transactional
public class JpaCartRepositoryAdapter implements CartRepository {
    private final SpringDataCartRepository data;
    private final CartPersistenceMapper mapper = new CartPersistenceMapper();
    @PersistenceContext private EntityManager em;
    public JpaCartRepositoryAdapter(SpringDataCartRepository data) {
        this.data = data;
    }
    @Override public Optional<Cart> findActiveByCustomerId(String customerId) { return data.findByCustomerId(customerId).map(mapper::toDomain); }
    @Override public void save(Cart model) {
        Objects.requireNonNull(model);
        data.lockById(model.getId());
        em.merge(mapper.toEntity(model));
        em.flush();
    }
}

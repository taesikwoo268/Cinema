package cinema.infrastructure.persistence.adapters;

import cinema.application.common.interfaces.CustomerRepository;
import cinema.application.common.exceptions.ConflictException;
import cinema.domain.customer.Customer;
import cinema.infrastructure.persistence.entities.*;
import cinema.infrastructure.persistence.repositories.*;
import cinema.infrastructure.persistence.mappers.CustomerPersistenceMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Repository
@Profile("mysql")
@Transactional
public class JpaCustomerRepositoryAdapter implements CustomerRepository {
    private final SpringDataCustomerRepository data;
    private final CustomerPersistenceMapper mapper = new CustomerPersistenceMapper();
    @PersistenceContext private EntityManager em;
    public JpaCustomerRepositoryAdapter(SpringDataCustomerRepository data) {
        this.data = data;
    }
    @Override public Optional<Customer> findById(String id) {
        return data.lockById(id).map(mapper::toDomain);
    }
    @Override public Optional<Customer> findByUserId(String userId) { return data.findByUserId(userId).map(mapper::toDomain); }
    @Override public void save(Customer model) {
        Objects.requireNonNull(model);
        if (data.existsById(model.getId())) throw new ConflictException("Customer ID already exists");
        em.persist(mapper.toEntity(model));
        em.flush();
    }
}

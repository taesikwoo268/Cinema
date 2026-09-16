package cinema.infrastructure.persistence.adapters;

import java.util.List;
import java.util.Optional;
import java.time.Instant;
import cinema.domain.customer.Customer;
import cinema.application.common.interfaces.CustomerRepository;
/** Unwired until persistence mapping and transactional contracts are implemented. */
public final class JpaCustomerRepositoryAdapter implements CustomerRepository {
    @Override public Optional<Customer> findById(String id) { throw new UnsupportedOperationException("Architecture Contract v0.1: not implemented" ); }
    @Override public Optional<Customer> findByUserId(String userId) { throw new UnsupportedOperationException("Architecture Contract v0.1: not implemented" ); }
    @Override public void save(Customer customer) { throw new UnsupportedOperationException("Architecture Contract v0.1: not implemented" ); }
}

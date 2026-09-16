package cinema.application.common.interfaces;

import java.util.List;
import java.util.Optional;
import java.time.Instant;
import cinema.domain.customer.Customer;
public interface CustomerRepository {
    Optional<Customer> findById(String id);
    Optional<Customer> findByUserId(String userId);
    void save(Customer customer);
}

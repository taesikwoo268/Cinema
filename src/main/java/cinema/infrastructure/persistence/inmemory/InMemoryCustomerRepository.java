package cinema.infrastructure.persistence.inmemory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import cinema.application.common.exceptions.ConflictException;
import cinema.application.common.interfaces.CustomerRepository;
import cinema.domain.customer.Customer;

public final class InMemoryCustomerRepository implements CustomerRepository {

    private final Map<String, Customer> customers = new HashMap<>();

    @Override
    public synchronized Optional<Customer> findById(String id) {
        return Optional.ofNullable(customers.get(id));
    }

    @Override
    public synchronized Optional<Customer> findByUserId(String userId) {
        return customers.values().stream()
                .filter(customer -> customer.getUserId().equals(userId))
                .findFirst();
    }

    @Override
    public synchronized void save(Customer customer) {
        if (customers.containsKey(customer.getId())) {
            throw new ConflictException("Customer ID already exists: " + customer.getId());
        }
        if (findByUserId(customer.getUserId()).isPresent()) {
            throw new ConflictException("Customer user ID already exists: " + customer.getUserId());
        }
        customers.put(customer.getId(), customer);
    }
}

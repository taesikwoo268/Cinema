package cinema.application.identity.commands;

import cinema.application.common.exceptions.ConflictException;
import cinema.application.common.exceptions.ValidationException;
import cinema.application.common.interfaces.CustomerRepository;
import cinema.application.common.interfaces.IdentityService;
import cinema.application.common.interfaces.UseCase;
import cinema.domain.common.DomainException;
import cinema.domain.customer.Customer;

import java.util.function.Supplier;

public class RegisterCommandHandler implements UseCase<RegisterCommand, String> {

    private final IdentityService identities;
    private final CustomerRepository customers;
    private final Supplier<String> idGenerator;

    public RegisterCommandHandler(IdentityService identities, CustomerRepository customers, Supplier<String> idGenerator) {
        this.identities = identities;
        this.customers = customers;
        this.idGenerator = idGenerator;
    }

    @Override public String handle(RegisterCommand input) {

        validate(input);

        String userId = identities.register(input.email(), input.password());
        if (userId == null || userId.isBlank()) {
            throw new ValidationException("UserId must not be blank");
        }
        if (customers.findByUserId(userId).isPresent()) {
            throw new ConflictException("Customer already exists for user: " + userId);
        }

        String customerID = idGenerator.get();
        Customer customer = createCustomer(customerID, userId, input.displayName());
        customers.save(customer);

        return customer.getId();
    }

    private static void validate(RegisterCommand input) {
        if (input == null) {
            throw new ValidationException("RegisterCommand must not be null");
        }
        if (input.email() == null || input.email().isBlank()) {
            throw new ValidationException("email must not be blank");
        }
        if (input.password() == null || input.password().isBlank()) {
            throw new ValidationException("password must not be blank");
        }
        if (input.displayName() == null || input.displayName().isBlank()) {
            throw new ValidationException("DisplayName must not be blank");
        }
    }

    private Customer createCustomer(String id,String userId, String displayName) {
        try {
            return new Customer(id, userId, displayName);
        } catch (DomainException ex) {
            throw new ValidationException(ex.getMessage());
        }
    }
}

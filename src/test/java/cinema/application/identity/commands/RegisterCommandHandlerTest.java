package cinema.application.identity.commands;

import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import cinema.application.common.exceptions.ConflictException;
import cinema.application.common.exceptions.ValidationException;
import cinema.application.common.interfaces.CustomerRepository;
import cinema.application.common.interfaces.IdentityService;
import cinema.domain.customer.Customer;

public class RegisterCommandHandlerTest {

    private final IdentityService identities = mock(IdentityService.class);
    private final CustomerRepository customers = mock(CustomerRepository.class);
    private int generatedIds;
    private final Supplier<String> idGenerator = () -> {
        generatedIds++;
        return "customer-1";
    };

    RegisterCommandHandler handler = new RegisterCommandHandler(identities, customers, idGenerator);

    @Test
    void validRegisterReturnsGeneratedCustomerId() {

        when(identities.register("abc@gmail.com", "xyz123"))
                .thenReturn("user-123");

        when(customers.findByUserId("user-123")).thenReturn(Optional.empty());

        assertEquals("customer-1", handler.handle(new RegisterCommand("abc@gmail.com",
                "xyz123", "name456")));

    }

    @Test
    void validRegisterCallsIdentityExactlyAsExpected() {

        when(identities.register("abc@gmail.com", "xyz123")).
                thenReturn("user-123");

        when(customers.findByUserId("user-123")).thenReturn(Optional.empty());

        handler.handle(new RegisterCommand("abc@gmail.com",
                "xyz123", "name456"));

        verify(identities, times(1)).
                register("abc@gmail.com", "xyz123");

    }

    @Test
    void savedCustomerHasCorrectIdUserIdDisplayName() {

        when(identities.register("abc@gmail.com", "xyz123"))
                .thenReturn("user-123");

        when(customers.findByUserId("user-123")).thenReturn(Optional.empty());

        handler.handle(new RegisterCommand("abc@gmail.com",
                "xyz123", "name456"));

        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);

        verify(customers, times(1)).findByUserId("user-123");
        verify(customers, times(1)).save(captor.capture());
        verifyNoMoreInteractions(customers);

        Customer savedCustomer = captor.getValue();

        assertEquals("user-123", savedCustomer.getUserId());
        assertEquals("name456", savedCustomer.getDisplayName());
        assertEquals("customer-1", savedCustomer.getId());
    }

    private void assertInvalid(RegisterCommand command) {
        assertThrows(ValidationException.class, () -> handler.handle(command));
    }

    @Test
    void rejectsNullCommand() {
        assertInvalid(null);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void rejectsNullOrEmptyEmail(String email) {
        assertInvalid(new RegisterCommand(email, "xyz456", "name123"));
    }

    @Test
    void rejectsBlankEmail() {
        assertInvalid(new RegisterCommand(" \t\n", "xyz456", "name123"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void rejectsNullOrEmptyPassword(String password) {
        assertInvalid(new RegisterCommand("abc@gmail.com", password, "name123"));
    }

    @Test
    void rejectsBlankPassword() {
        assertInvalid(new RegisterCommand("abc@gmail.com", " \t\n", "name123"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void rejectsNullOrEmptyDisplayName(String displayName) {
        assertInvalid(new RegisterCommand(
                "abc@gmail.com",
                "xyz456",
                displayName));
    }

    @Test
    void rejectsBlankDisplayName() {
        assertInvalid(new RegisterCommand("abc@gmail.com", "xyz456", " \t\n"));
    }

    @Test
    void validationCausesZeroSideEffects() {
        assertInvalid(null);

        verifyNoInteractions(identities, customers);
        assertEquals(0, generatedIds);
    }

    @Test
    void existingCustomerForReturnedUserId() {
        Customer customer = new Customer("customer-x", "user-123", "namex");

        when(identities.register("abc@gmail.com", "xyz123"))
                .thenReturn("user-123");

        when(customers.findByUserId("user-123")).thenReturn(Optional.of(customer));

        assertThrows(ConflictException.class,
                () -> handler.handle(new RegisterCommand("abc@gmail.com",
                        "xyz123", "name456")));

    }

    @Test
    void IdGeneratorNotCalledBeforeRequiredUserIdCheck() {

        when(identities.register("abc@gmail.com", "xyz123"))
                .thenReturn("");

        assertThrows(ValidationException.class,
                () -> handler.handle(new RegisterCommand("abc@gmail.com",
                        "xyz123", "name456")));

        assertEquals(0, generatedIds);

    }

    @Test
    void IdGeneratorNotCalledBeforeRequiredCustomerExistsCheck() {
        Customer customer = new Customer("customer-x", "user-123", "namex");

        when(identities.register("abc@gmail.com", "xyz123"))
                .thenReturn("user-123");

        when(customers.findByUserId("user-123")).thenReturn(Optional.of(customer));

        assertThrows(ConflictException.class,
                () -> handler.handle(new RegisterCommand("abc@gmail.com",
                        "xyz123", "name456")));

        assertEquals(0, generatedIds);
    }

    @Test
    void SaveNotBeforeRequiredUserIdCheck() {

        when(identities.register("abc@gmail.com", "xyz123"))
                .thenReturn("");

        assertThrows(ValidationException.class,
                () -> handler.handle(new RegisterCommand("abc@gmail.com",
                        "xyz123", "name456")));

        verify(customers, never()).save(any(Customer.class));

    }

    @Test
    void SaveNotBeforeRequiredCustomerExistsCheck() {

        Customer customer = new Customer("customer-x", "user-123", "namex");

        when(identities.register("abc@gmail.com", "xyz123"))
                .thenReturn("user-123");

        when(customers.findByUserId("user-123")).thenReturn(Optional.of(customer));

        assertThrows(ConflictException.class,
                () -> handler.handle(new RegisterCommand("abc@gmail.com",
                        "xyz123", "name456")));

        verify(customers, never()).save(any(Customer.class));

    }

    @Test
    void repositoryConflictPropagates() {

        when(identities.register("abc@gmail.com", "xyz123"))
                .thenReturn("user-123");

        when(customers.findByUserId("user-123")).thenReturn(Optional.empty());

        doThrow(new ConflictException("Customer already exists for user: " + "user-123")).
                when(customers).save(any(Customer.class));

        assertThrows(ConflictException.class, ()
                -> handler.handle(new RegisterCommand("abc@gmail.com",
                        "xyz123", "name456")));

    }
}

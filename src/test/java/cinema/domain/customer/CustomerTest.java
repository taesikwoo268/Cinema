package cinema.domain.customer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import cinema.domain.common.BaseEntity;
import cinema.domain.common.DomainException;

class CustomerTest {

    private static final List<String> CREDENTIAL_MARKERS =
            List.of("password", "credential", "secret", "token", "hash");

    @Test
    void validCustomerKeepsValues() {
        Customer customer = new Customer("c1", "u1", "Bao Long");

        assertEquals("c1", customer.getId());
        assertEquals("u1", customer.getUserId());
        assertEquals("Bao Long", customer.getDisplayName());
    }

    @Test
    void idComesFromBaseEntityAndCannotBeRedefined() throws NoSuchMethodException {
        Method getId = Customer.class.getMethod("getId");

        assertSame(BaseEntity.class, getId.getDeclaringClass());
        assertTrue(Modifier.isFinal(getId.getModifiers()));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void rejectsMissingOrBlankId(String id) {
        DomainException exception = assertThrows(DomainException.class,
                () -> new Customer(id, "u1", "Bao Long"));

        assertEquals("Entity ID must not be blank", exception.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void rejectsMissingOrBlankUserId(String userId) {
        DomainException exception = assertThrows(DomainException.class,
                () -> new Customer("c1", userId, "Bao Long"));

        assertEquals("User ID must not be blank", exception.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void rejectsMissingOrBlankDisplayName(String displayName) {
        DomainException exception = assertThrows(DomainException.class,
                () -> new Customer("c1", "u1", displayName));

        assertEquals("Display name must not be blank", exception.getMessage());
    }

    @Test
    void trimsDisplayName() {
        assertEquals("Bao Long", new Customer("c1", "u1", "  Bao Long  ").getDisplayName());
        assertEquals("Bao Long", new Customer("c1", "u1", "\tBao Long\n").getDisplayName());
    }

    @Test
    void gettersPreserveValuesWithoutExtraNormalization() {
        Customer customer = new Customer(" c1 ", " u1 ", " Bao Long ");

        assertEquals(" c1 ", customer.getId());
        assertEquals(" u1 ", customer.getUserId());
        assertEquals("Bao Long", customer.getDisplayName());
    }

    @Test
    void rejectsBlankIdBeforeOtherInvariants() {
        assertThrows(DomainException.class, () -> new Customer("  ", " ", " "));
    }

    @Test
    void exposesNoCredentialState() {
        for (Field field : Customer.class.getDeclaredFields()) {
            assertFalse(containsCredentialMarker(field.getName()),
                    "Customer must not store credential state: " + field.getName());
        }
        for (Method method : Customer.class.getDeclaredMethods()) {
            assertFalse(containsCredentialMarker(method.getName()),
                    "Customer must not expose credential state: " + method.getName());
        }
    }

    private static boolean containsCredentialMarker(String name) {
        String lowered = name.toLowerCase();
        return CREDENTIAL_MARKERS.stream().anyMatch(lowered::contains);
    }
}

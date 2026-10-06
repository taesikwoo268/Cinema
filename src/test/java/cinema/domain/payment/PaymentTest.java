package cinema.domain.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cinema.domain.common.BaseEntity;
import cinema.domain.common.DomainException;
import cinema.domain.common.Money;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class PaymentTest {

    private static final Currency VND = Currency.getInstance("VND");

    private Money money(String amount) {
        return new Money(new BigDecimal(amount), VND);
    }

    private Payment payment(String bookingId, Money amount, PaymentStatus status, String providerReference) {
        return new Payment("p1", bookingId, amount, status, providerReference);
    }

    @Test
    void validSuccessPaymentKeepsValues() {
        Money amount = money("125000.50");
        Payment payment = payment("b1", amount, PaymentStatus.SUCCESS, "ref-123");

        assertEquals("p1", payment.getId());
        assertEquals("b1", payment.getBookingId());
        assertEquals(amount, payment.getAmount());
        assertEquals(PaymentStatus.SUCCESS, payment.getStatus());
        assertEquals("ref-123", payment.getProviderReference());
    }

    @Test
    void validFailedPaymentKeepsValues() {
        Money amount = money("0");
        Payment payment = payment("b1", amount, PaymentStatus.FAILED, "ref-999");

        assertEquals("p1", payment.getId());
        assertEquals("b1", payment.getBookingId());
        assertEquals(amount, payment.getAmount());
        assertEquals(PaymentStatus.FAILED, payment.getStatus());
        assertEquals("ref-999", payment.getProviderReference());
    }

    @Test
    void pendingAllowsNullProviderReference() {
        Payment payment = payment("b1", money("10000"), PaymentStatus.PENDING, null);

        assertEquals(PaymentStatus.PENDING, payment.getStatus());
        assertNull(payment.getProviderReference());
    }

    @Test
    void failedAllowsNullProviderReference() {
        Payment payment = payment("b1", money("10000"), PaymentStatus.FAILED, null);

        assertEquals(PaymentStatus.FAILED, payment.getStatus());
        assertNull(payment.getProviderReference());
    }

    @Test
    void rejectsNullBookingId() {
        assertThrows(DomainException.class,
                () -> payment(null, money("1"), PaymentStatus.PENDING, null));
    }

    @Test
    void rejectsEmptyBookingId() {
        assertThrows(DomainException.class,
                () -> payment("", money("1"), PaymentStatus.PENDING, null));
    }

    @Test
    void rejectsBlankBookingId() {
        assertThrows(DomainException.class,
                () -> payment(" \t\n", money("1"), PaymentStatus.PENDING, null));
    }

    @Test
    void rejectsNullAmount() {
        assertThrows(DomainException.class,
                () -> payment("b1", null, PaymentStatus.PENDING, null));
    }

    @Test
    void rejectsNullStatus() {
        assertThrows(DomainException.class,
                () -> payment("b1", money("1"), null, null));
    }

    @Test
    void successRejectsNullProviderReference() {
        assertThrows(DomainException.class,
                () -> payment("b1", money("1"), PaymentStatus.SUCCESS, null));
    }

    @Test
    void successRejectsEmptyProviderReference() {
        assertThrows(DomainException.class,
                () -> payment("b1", money("1"), PaymentStatus.SUCCESS, ""));
    }

    @Test
    void successRejectsBlankProviderReference() {
        assertThrows(DomainException.class,
                () -> payment("b1", money("1"), PaymentStatus.SUCCESS, " \t\n"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void refundedRejectsMissingOrBlankProviderReference(String providerReference) {
        assertThrows(DomainException.class,
                () -> payment("b1", money("1"), PaymentStatus.REFUNDED, providerReference));
    }

    @Test
    void keepsAmountObjectExactlyWithoutRounding() {
        Money amount = money("125000.555");
        Payment payment = payment("b1", amount, PaymentStatus.SUCCESS, "ref-123");

        assertSame(amount, payment.getAmount());
        assertEquals(new BigDecimal("125000.555"), payment.getAmount().amount());
        assertEquals(VND, payment.getAmount().currency());
    }

    @Test
    void idComesFromBaseEntityAndCannotBeRedefined() throws NoSuchMethodException {
        Method getId = Payment.class.getMethod("getId");

        assertSame(BaseEntity.class, getId.getDeclaringClass());
        assertTrue(Modifier.isFinal(getId.getModifiers()));
    }

    @Test
    void rejectsBlankIdBeforeOtherInvariants() {
        assertThrows(DomainException.class,
                () -> new Payment(" ", "b1", money("1"), PaymentStatus.SUCCESS, "ref-123"));
    }

    @Test
    void fieldsAreFinalAndNoStateTransitionsAreExposed() {
        for (Field field : Payment.class.getDeclaredFields()) {
            assertTrue(Modifier.isFinal(field.getModifiers()), field.getName() + " must be final");
        }
        List<String> forbidden = List.of("setStatus", "markSuccess", "markFailed");
        for (Method method : Payment.class.getDeclaredMethods()) {
            assertFalse(forbidden.contains(method.getName()),
                    "Payment must stay immutable: " + method.getName());
            assertFalse(method.getName().startsWith("set"),
                    "Payment must not expose setters: " + method.getName());
        }
    }
}
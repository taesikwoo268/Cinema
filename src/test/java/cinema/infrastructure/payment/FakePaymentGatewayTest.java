package cinema.infrastructure.payment;

import cinema.domain.common.Money;
import java.math.BigDecimal;
import java.util.Currency;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FakePaymentGatewayTest {
    private static final Money AMOUNT = new Money(new BigDecimal("100000"), Currency.getInstance("VND"));

    @Test
    void payReturnsSuccessfulResultWithFakeProviderReference() {
        var result = new FakePaymentGateway().pay("booking-1", AMOUNT);

        assertTrue(result.successful());
        assertFalse(result.providerReference().isBlank());
        assertTrue(result.providerReference().startsWith("FAKE-PAY-"));
    }

    @Test
    void payGeneratesUniqueSequentialReferencesPerGatewayInstance() {
        var gateway = new FakePaymentGateway();

        var first = gateway.pay("booking-1", AMOUNT);
        var second = gateway.pay("booking-2", AMOUNT);

        assertNotEquals(first.providerReference(), second.providerReference());
        assertTrue(first.providerReference().endsWith("1"));
        assertTrue(second.providerReference().endsWith("2"));
    }

    @Test
    void payRejectsNullOrBlankBookingIdAndNullAmount() {
        var gateway = new FakePaymentGateway();

        assertThrows(IllegalArgumentException.class, () -> gateway.pay(null, AMOUNT));
        assertThrows(IllegalArgumentException.class, () -> gateway.pay("  ", AMOUNT));
        assertThrows(IllegalArgumentException.class, () -> gateway.pay("booking-1", null));
    }

    @Test
    void refundReturnsSuccessfulResultWithFakeRefundReference() {
        var result = new FakePaymentGateway().refund("FAKE-PAY-1", AMOUNT);

        assertTrue(result.successful());
        assertFalse(result.providerReference().isBlank());
        assertTrue(result.providerReference().startsWith("FAKE-REFUND-"));
    }

    @Test
    void refundRejectsNullOrBlankPaymentReferenceAndNullAmount() {
        var gateway = new FakePaymentGateway();

        assertThrows(IllegalArgumentException.class, () -> gateway.refund(null, AMOUNT));
        assertThrows(IllegalArgumentException.class, () -> gateway.refund(" ", AMOUNT));
        assertThrows(IllegalArgumentException.class, () -> gateway.refund("FAKE-PAY-1", null));
    }
}

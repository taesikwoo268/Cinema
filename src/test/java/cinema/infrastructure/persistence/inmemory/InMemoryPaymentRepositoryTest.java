package cinema.infrastructure.persistence.inmemory;

import cinema.domain.common.Money;
import cinema.domain.payment.Payment;
import cinema.domain.payment.PaymentStatus;
import java.math.BigDecimal;
import java.util.Currency;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryPaymentRepositoryTest {
    private static final Money AMOUNT = new Money(new BigDecimal("100000"), Currency.getInstance("VND"));

    @Test
    void newRepositoryDoesNotFindUnknownId() {
        var repository = new InMemoryPaymentRepository();

        assertTrue(repository.findById("payment-1").isEmpty());
    }

    @Test
    void saveMakesPaymentAvailableById() {
        var repository = new InMemoryPaymentRepository();
        var payment = payment("payment-1", "booking-1", "reference-1");

        repository.save(payment);

        assertSame(payment, repository.findById("payment-1").orElseThrow());
    }

    @Test
    void saveRejectsNullPayment() {
        var repository = new InMemoryPaymentRepository();

        assertThrows(NullPointerException.class, () -> repository.save(null));
    }

    @Test
    void findByBookingIdReturnsOnlyPaymentsForRequestedBooking() {
        var repository = new InMemoryPaymentRepository();
        var first = payment("payment-1", "booking-1", "reference-1");
        var second = payment("payment-2", "booking-1", "reference-2");
        repository.save(first);
        repository.save(payment("payment-3", "booking-2", "reference-3"));
        repository.save(second);

        assertEquals(java.util.List.of(first, second), repository.findByBookingId("booking-1"));
        assertTrue(repository.findByBookingId("booking-missing").isEmpty());
    }

    @Test
    void findByBookingIdPreservesInsertionOrder() {
        var repository = new InMemoryPaymentRepository();
        var first = payment("payment-2", "booking-1", "reference-2");
        var second = payment("payment-1", "booking-1", "reference-1");
        repository.save(first);
        repository.save(second);

        assertEquals(java.util.List.of(first, second), repository.findByBookingId("booking-1"));
    }

    @Test
    void saveWithExistingIdUpdatesStoredPayment() {
        var repository = new InMemoryPaymentRepository();
        repository.save(payment("payment-1", "booking-1", "reference-1"));
        var replacement = payment("payment-1", "booking-2", "reference-2");

        repository.save(replacement);

        assertSame(replacement, repository.findById("payment-1").orElseThrow());
        assertEquals(java.util.List.of(replacement), repository.findByBookingId("booking-2"));
        assertTrue(repository.findByBookingId("booking-1").isEmpty());
    }

    @Test
    void returnedListCannotModifyRepositoryState() {
        var repository = new InMemoryPaymentRepository();
        var payment = payment("payment-1", "booking-1", "reference-1");
        repository.save(payment);
        var result = repository.findByBookingId("booking-1");

        assertThrows(UnsupportedOperationException.class, result::clear);
        assertEquals(java.util.List.of(payment), repository.findByBookingId("booking-1"));
    }

    private static Payment payment(String id, String bookingId, String reference) {
        return new Payment(id, bookingId, AMOUNT, PaymentStatus.SUCCESS, reference);
    }
}

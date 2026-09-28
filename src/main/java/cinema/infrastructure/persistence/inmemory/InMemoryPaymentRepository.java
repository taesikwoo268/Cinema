package cinema.infrastructure.persistence.inmemory;

import cinema.application.common.interfaces.PaymentRepository;
import cinema.domain.payment.Payment;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** In-memory payment repository with deterministic insertion-order queries. */
public final class InMemoryPaymentRepository implements PaymentRepository {
    private final LinkedHashMap<String, Payment> payments = new LinkedHashMap<>();

    @Override
    public synchronized Optional<Payment> findById(String id) {
        return Optional.ofNullable(payments.get(id));
    }

    @Override
    public synchronized List<Payment> findByBookingId(String bookingId) {
        return payments.values().stream()
                .filter(payment -> payment.getBookingId().equals(bookingId))
                .toList();
    }

    @Override
    public synchronized void save(Payment payment) {
        Objects.requireNonNull(payment, "Payment must not be null");
        payments.put(payment.getId(), payment);
    }
}

package cinema.infrastructure.persistence.adapters;

import java.util.List;
import java.util.Optional;
import java.time.Instant;
import cinema.domain.payment.Payment;
import cinema.application.common.interfaces.PaymentRepository;
/** Unwired until persistence mapping and transactional contracts are implemented. */
public final class JpaPaymentRepositoryAdapter implements PaymentRepository {
    @Override public Optional<Payment> findById(String id) { throw new UnsupportedOperationException("Architecture Contract v0.1: not implemented" ); }
    @Override public List<Payment> findByBookingId(String bookingId) { throw new UnsupportedOperationException("Architecture Contract v0.1: not implemented" ); }
    @Override public void save(Payment payment) { throw new UnsupportedOperationException("Architecture Contract v0.1: not implemented" ); }
}

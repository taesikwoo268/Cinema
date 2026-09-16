package cinema.application.common.interfaces;

import java.util.List;
import java.util.Optional;
import java.time.Instant;
import cinema.domain.payment.Payment;
public interface PaymentRepository {
    Optional<Payment> findById(String id);
    List<Payment> findByBookingId(String bookingId);
    void save(Payment payment);
}

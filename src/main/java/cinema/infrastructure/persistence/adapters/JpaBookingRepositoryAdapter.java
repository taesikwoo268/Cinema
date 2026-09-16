package cinema.infrastructure.persistence.adapters;

import java.util.List;
import java.util.Optional;
import java.time.Instant;
import cinema.domain.booking.Booking;
import cinema.application.common.interfaces.BookingRepository;
/** Unwired until persistence mapping and transactional contracts are implemented. */
public final class JpaBookingRepositoryAdapter implements BookingRepository {
    @Override public Optional<Booking> findById(String id) { throw new UnsupportedOperationException("Architecture Contract v0.1: not implemented" ); }
    @Override public List<Booking> findByCustomerId(String customerId) { throw new UnsupportedOperationException("Architecture Contract v0.1: not implemented" ); }
    @Override public void save(Booking booking) { throw new UnsupportedOperationException("Architecture Contract v0.1: not implemented" ); }
}

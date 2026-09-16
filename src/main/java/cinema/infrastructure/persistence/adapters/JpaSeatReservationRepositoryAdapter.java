package cinema.infrastructure.persistence.adapters;

import java.util.List;
import java.util.Optional;
import java.time.Instant;
import cinema.application.common.interfaces.SeatReservationRepository;
/** Unwired until persistence mapping and transactional contracts are implemented. */
public final class JpaSeatReservationRepositoryAdapter implements SeatReservationRepository {
    @Override public boolean isBooked(String screeningId, String seatId) { throw new UnsupportedOperationException("Architecture Contract v0.1: not implemented" ); }
    @Override public void markBooked(String screeningId, String seatId, String bookingId) { throw new UnsupportedOperationException("Architecture Contract v0.1: not implemented" ); }
}

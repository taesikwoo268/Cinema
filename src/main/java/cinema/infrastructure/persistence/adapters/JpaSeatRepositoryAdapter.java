package cinema.infrastructure.persistence.adapters;

import java.util.List;
import java.util.Optional;
import java.time.Instant;
import cinema.domain.cinema.Seat;
import cinema.application.common.interfaces.SeatRepository;
/** Unwired until persistence mapping and transactional contracts are implemented. */
public final class JpaSeatRepositoryAdapter implements SeatRepository {
    @Override public Optional<Seat> findById(String id) { throw new UnsupportedOperationException("Architecture Contract v0.1: not implemented" ); }
    @Override public List<Seat> findByHallId(String hallId) { throw new UnsupportedOperationException("Architecture Contract v0.1: not implemented" ); }
}

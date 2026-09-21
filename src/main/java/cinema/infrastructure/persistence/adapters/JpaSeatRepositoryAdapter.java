package cinema.infrastructure.persistence.adapters;

import java.util.List;
import java.util.Optional;

import cinema.application.common.interfaces.SeatRepository;
import cinema.domain.cinema.Seat;

/**
 * Unwired until persistence mapping and transactional contracts are
 * implemented.
 */
public final class JpaSeatRepositoryAdapter implements SeatRepository {

    @Override
    public Optional<Seat> findById(String id) {
        throw new UnsupportedOperationException("Architecture Contract v0.1: not implemented");
    }

    @Override
    public List<Seat> findByHallId(String hallId) {
        throw new UnsupportedOperationException("Architecture Contract v0.1: not implemented");
    }

    @Override
    public boolean existsByHallIdAndRowAndNumber(String hallId, String row, int number) {
        throw new UnsupportedOperationException("Architecture Contract v0.1: not implemented");
    }

    @Override
    public void save(Seat seat) {
        throw new UnsupportedOperationException("Architecture Contract v0.1: not implemented");
    }
}

package cinema.infrastructure.persistence.adapters;

import java.util.List;
import java.util.Optional;
import java.time.Instant;
import cinema.domain.screening.Screening;
import cinema.application.common.interfaces.ScreeningRepository;
/** Unwired until persistence mapping and transactional contracts are implemented. */
public final class JpaScreeningRepositoryAdapter implements ScreeningRepository {
    @Override public Optional<Screening> findById(String id) { throw new UnsupportedOperationException("Architecture Contract v0.1: not implemented" ); }
    @Override public List<Screening> findByMovieId(String movieId) { throw new UnsupportedOperationException("Architecture Contract v0.1: not implemented" ); }
    @Override public boolean hasOverlap(String hallId, Instant start, Instant end) { throw new UnsupportedOperationException("Architecture Contract v0.1: not implemented" ); }
    @Override public void save(Screening screening) { throw new UnsupportedOperationException("Architecture Contract v0.1: not implemented" ); }
}

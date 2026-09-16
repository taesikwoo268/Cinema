package cinema.application.common.interfaces;

import java.util.List;
import java.util.Optional;
import java.time.Instant;
import cinema.domain.screening.Screening;
public interface ScreeningRepository {
    Optional<Screening> findById(String id);
    List<Screening> findByMovieId(String movieId);
    boolean hasOverlap(String hallId, Instant start, Instant end);
    void save(Screening screening);
}

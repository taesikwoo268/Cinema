package cinema.application.common.interfaces;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import cinema.domain.screening.Screening;

public interface ScreeningRepository {

    Optional<Screening> findById(String id);

    List<Screening> findByMovieId(String movieId);

    /**
     * Checks overlap within the specified hall using half-open intervals:
     * existing.startTime < end and existing.endTime > start. Adjacent intervals
     * (existing.endTime == start or existing.startTime == end) do not overlap.
     */
    boolean hasOverlap(String hallId, Instant start, Instant end);

    void save(Screening screening);
}

package cinema.infrastructure.persistence.inmemory;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import cinema.application.common.interfaces.ScreeningRepository;
import cinema.domain.screening.Screening;

public final class InMemoryScreeningRepository implements ScreeningRepository {

    private final Map<String, Screening> screenings = new LinkedHashMap<>();

    @Override
    public synchronized Optional<Screening> findById(String id) {
        return Optional.ofNullable(screenings.get(id));
    }

    @Override
    public synchronized List<Screening> findByMovieId(String movieId) {
        return screenings.values().stream()
                .filter(screening -> screening.getMovieId().equals(movieId))
                .toList();
    }

    @Override
    public synchronized boolean hasOverlap(String hallId, Instant start, Instant end) {
        return screenings.values().stream()
                .filter(screening -> screening.getHallId().equals(hallId))
                .anyMatch(screening -> screening.getStartTime().isBefore(end)
                && screening.getEndTime().isAfter(start));
    }

    @Override
    public synchronized void save(Screening screening) {
        screenings.put(screening.getId(), screening);
    }
}

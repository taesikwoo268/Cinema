package cinema.infrastructure.persistence.inmemory;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import cinema.application.common.exceptions.ConflictException;
import cinema.application.common.interfaces.CinemaRepository;
import cinema.domain.cinema.Cinema;

public final class InMemoryCinemaRepository implements CinemaRepository {

    private final Map<String, Cinema> cinemas = new HashMap<>();

    @Override
    public synchronized Optional<Cinema> findById(String id) {
        return Optional.ofNullable(cinemas.get(id));
    }

    @Override
    public synchronized List<Cinema> findAll() {
        return cinemas.values().stream().sorted(Comparator.comparing(Cinema::getId)).toList();
    }

    @Override
    public synchronized void save(Cinema cinema) {
        if (cinemas.containsKey(cinema.getId())) {
            throw new ConflictException("Cinema ID already exists: " + cinema.getId());
        }
        cinemas.put(cinema.getId(), cinema);
    }
}
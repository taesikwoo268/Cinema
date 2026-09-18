package cinema.infrastructure.persistence.inmemory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import cinema.application.common.exceptions.ConflictException;
import cinema.application.common.interfaces.HallRepository;
import cinema.domain.cinema.Hall;

public final class InMemoryHallRepository implements HallRepository {

    private final Map<String, Hall> halls = new HashMap<>();

    @Override
    public synchronized Optional<Hall> findById(String id) {
        return Optional.ofNullable(halls.get(id));
    }

    @Override
    public synchronized List<Hall> findByCinemaId(String cinemaId) {
        return halls.values().stream()
                .filter(hall -> hall.getCinemaId().equals(cinemaId))
                .sorted(java.util.Comparator.comparing(Hall::getName).thenComparing(Hall::getId))
                .toList();
    }

    @Override
    public synchronized boolean existsByCinemaIdAndName(String cinemaId, String name) {
        return halls.values().stream().anyMatch(hall -> hall.getCinemaId().equals(cinemaId) && hall.getName().equals(name));
    }

    @Override
    public synchronized void save(Hall hall) {
        if (halls.containsKey(hall.getId())) {
            throw new ConflictException("Hall ID already exists: " + hall.getId());
        }
        if (existsByCinemaIdAndName(hall.getCinemaId(), hall.getName())) {
            throw new ConflictException("Hall name already exists in cinema");
        }
        halls.put(hall.getId(), hall);
    }
}
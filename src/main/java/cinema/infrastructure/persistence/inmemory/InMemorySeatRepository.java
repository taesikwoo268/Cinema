package cinema.infrastructure.persistence.inmemory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import cinema.application.common.exceptions.ConflictException;
import cinema.application.common.interfaces.SeatRepository;
import cinema.domain.cinema.Seat;

public final class InMemorySeatRepository implements SeatRepository {

    private final Map<String, Seat> seats = new HashMap<>();

    @Override
    public synchronized Optional<Seat> findById(String id) {
        return Optional.ofNullable(seats.get(id));
    }

    @Override
    public synchronized List<Seat> findByHallId(String hallId) {
        return seats.values().stream()
                .filter(seat -> seat.getHallId().equals(hallId))
                .sorted(java.util.Comparator.comparing(Seat::getRow)
                        .thenComparingInt(Seat::getNumber).thenComparing(Seat::getId))
                .toList();
    }

    @Override
    public synchronized boolean existsByHallIdAndRowAndNumber(String hallId, String row, int number) {
        return seats.values().stream().anyMatch(seat -> seat.getHallId().equals(hallId)
                && seat.getRow().equals(row) && seat.getNumber() == number);
    }

    @Override
    public synchronized void save(Seat seat) {
        if (seats.containsKey(seat.getId())) {
            throw new ConflictException("Seat ID already exists: " + seat.getId());
        }
        if (existsByHallIdAndRowAndNumber(seat.getHallId(), seat.getRow(), seat.getNumber())) {
            throw new ConflictException("Seat position already exists in hall");
        }
        seats.put(seat.getId(), seat);
    }
}
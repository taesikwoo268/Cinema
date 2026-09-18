package cinema.application.common.interfaces;

import java.util.List;
import java.util.Optional;

import cinema.domain.cinema.Seat;

public interface SeatRepository {

    Optional<Seat> findById(String id);

    List<Seat> findByHallId(String hallId);

    boolean existsByHallIdAndRowAndNumber(String hallId, String row, int number);

    void save(Seat seat);
}
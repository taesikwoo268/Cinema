package cinema.application.common.interfaces;

import java.util.List;
import java.util.Optional;
import java.time.Instant;
import cinema.domain.cinema.Seat;
public interface SeatRepository {
    Optional<Seat> findById(String id);
    List<Seat> findByHallId(String hallId);
}

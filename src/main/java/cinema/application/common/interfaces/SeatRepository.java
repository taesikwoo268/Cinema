package cinema.application.common.interfaces;

import java.util.List;
import java.util.Optional;

import cinema.domain.cinema.Seat;

/**
 * Output port for physical seats.
 *
 * <p>
 * {@link #save(Seat)} is add-only in M1: it rejects a duplicate ID or a
 * duplicate business key {@code (hallId, row, number)} with
 * {@link cinema.application.common.exceptions.ConflictException}. The unique
 * check and the write must be atomic inside a single repository instance.
 * Application may pre-check for an early error, but the adapter still enforces
 * the rule. List results are snapshots ordered by row, then number, then ID.
 */
public interface SeatRepository {

    Optional<Seat> findById(String id);

    List<Seat> findByHallId(String hallId);

    boolean existsByHallIdAndRowAndNumber(String hallId, String row, int number);

    void save(Seat seat);
}

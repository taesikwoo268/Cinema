package cinema.domain.screening;

import cinema.domain.common.BaseEntity;
import cinema.domain.common.Money;
import java.time.Instant;
import java.util.List;

/** Domain state skeleton; invariants and transitions are intentionally pending. */
public final class Screening extends BaseEntity {
    private final String movieId;
    private final String hallId;
    private final Instant startTime;
    private final Instant endTime;
    private final Money basePrice;
    private final ScreeningStatus status;
    public Screening(String id, String movieId, String hallId, Instant startTime, Instant endTime, Money basePrice, ScreeningStatus status) {
        super(id);
        this.movieId = movieId;
        this.hallId = hallId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.basePrice = basePrice;
        this.status = status;
    }
    public String getMovieId() { return movieId; }
    public String getHallId() { return hallId; }
    public Instant getStartTime() { return startTime; }
    public Instant getEndTime() { return endTime; }
    public Money getBasePrice() { return basePrice; }
    public ScreeningStatus getStatus() { return status; }
}

package cinema.domain.screening;

import java.time.Instant;

import cinema.domain.common.BaseEntity;
import cinema.domain.common.DomainException;
import cinema.domain.common.Money;

/**
 * Screening identity, time interval and price; cross-screening rules belong to
 * Application.
 */
public final class Screening extends BaseEntity {

    private final String movieId;
    private final String hallId;
    private final Instant startTime;
    private final Instant endTime;
    private final Money basePrice;
    private ScreeningStatus status;

    /**
     * Creates a new scheduled screening.
     */
    public Screening(String id, String movieId, String hallId, Instant startTime, Instant endTime, Money basePrice) {
        this(id, movieId, hallId, startTime, endTime, basePrice, ScreeningStatus.SCHEDULED);
    }

    /**
     * Preserves the explicit-status constructor for existing callers.
     */
    public Screening(String id, String movieId, String hallId, Instant startTime, Instant endTime, Money basePrice, ScreeningStatus status) {
        super(id);
        if (movieId == null || movieId.isBlank()) {
            throw new DomainException("Screening movieId must not be blank");
        }
        if (hallId == null || hallId.isBlank()) {
            throw new DomainException("Screening hallId must not be blank");
        }
        if (startTime == null) {
            throw new DomainException("Screening startTime must not be null");
        }
        if (endTime == null) {
            throw new DomainException("Screening endTime must not be null");
        }
        if (!startTime.isBefore(endTime)) {
            throw new DomainException("Screening startTime must be before endTime");
        }
        if (basePrice == null) {
            throw new DomainException("Screening basePrice must not be null");
        }
        // Money guarantees a non-negative amount.
        this.movieId = movieId;
        this.hallId = hallId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.basePrice = basePrice;
        this.status = status;
    }

    public String getMovieId() {
        return movieId;
    }

    public String getHallId() {
        return hallId;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public Money getBasePrice() {
        return basePrice;
    }

    public ScreeningStatus getStatus() {
        return status;
    }

    public void cancel() {
        if (status != ScreeningStatus.SCHEDULED) {
            throw new DomainException("Screening can only be cancelled when scheduled");
        }
        status = ScreeningStatus.CANCELLED;
    }
}

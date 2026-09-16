package cinema.domain.movie;

import cinema.domain.common.BaseEntity;
import cinema.domain.common.Money;
import java.time.Instant;
import java.util.List;

/** Domain state skeleton; invariants and transitions are intentionally pending. */
public final class Movie extends BaseEntity {
    private final String title;
    private final String description;
    private final int durationMinutes;
    private final String ageRating;
    private final MovieStatus status;
    public Movie(String id, String title, String description, int durationMinutes, String ageRating, MovieStatus status) {
        super(id);
        this.title = title;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.ageRating = ageRating;
        this.status = status;
    }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getDurationMinutes() { return durationMinutes; }
    public String getAgeRating() { return ageRating; }
    public MovieStatus getStatus() { return status; }
}

package cinema.domain.movie;

import cinema.domain.common.BaseEntity;
import cinema.domain.common.DomainException;

/** Movie metadata with required title and positive duration. */
public final class Movie extends BaseEntity {
    private final String title;
    private final String description;
    private final int durationMinutes;
    private final String ageRating;
    private final MovieStatus status;
    public Movie(String id, String title, String description, int durationMinutes, String ageRating, MovieStatus status) {
        super(id);
        if (title == null || title.isBlank()) {
            throw new DomainException("Movie title must not be blank");
        }
        if (durationMinutes <= 0) {
            throw new DomainException("Movie duration must be positive");
        }
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

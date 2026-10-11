package cinema.infrastructure.persistence.mappers;

import cinema.domain.movie.Movie;
import cinema.domain.common.Money;
import cinema.infrastructure.persistence.entities.*;
import java.util.Currency;

public final class MoviePersistenceMapper {
    public Movie toDomain(MovieJpaEntity e) {
        return new Movie(e.id, e.title, e.description, e.durationMinutes, e.ageRating, e.status);
    }
    public MovieJpaEntity toEntity(Movie model) {
        var e = new MovieJpaEntity();
        e.id = model.getId();
        e.title = model.getTitle();
        e.description = model.getDescription();
        e.durationMinutes = model.getDurationMinutes();
        e.ageRating = model.getAgeRating();
        e.status = model.getStatus();
        return e;
    }
}

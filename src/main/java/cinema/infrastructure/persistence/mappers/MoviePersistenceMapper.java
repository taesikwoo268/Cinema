package cinema.infrastructure.persistence.mappers;

import cinema.domain.movie.Movie;
import cinema.infrastructure.persistence.entities.MovieJpaEntity;
public interface MoviePersistenceMapper {
    Movie toDomain(MovieJpaEntity entity);
    MovieJpaEntity toEntity(Movie model);
}

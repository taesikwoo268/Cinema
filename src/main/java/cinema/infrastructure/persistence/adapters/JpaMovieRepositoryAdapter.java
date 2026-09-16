package cinema.infrastructure.persistence.adapters;

import java.util.List;
import java.util.Optional;
import java.time.Instant;
import cinema.domain.movie.Movie;
import cinema.application.common.interfaces.MovieRepository;
/** Unwired until persistence mapping and transactional contracts are implemented. */
public final class JpaMovieRepositoryAdapter implements MovieRepository {
    @Override public Optional<Movie> findById(String id) { throw new UnsupportedOperationException("Architecture Contract v0.1: not implemented" ); }
    @Override public List<Movie> findAll() { throw new UnsupportedOperationException("Architecture Contract v0.1: not implemented" ); }
    @Override public void save(Movie movie) { throw new UnsupportedOperationException("Architecture Contract v0.1: not implemented" ); }
    @Override public void deleteById(String id) { throw new UnsupportedOperationException("Architecture Contract v0.1: not implemented" ); }
}

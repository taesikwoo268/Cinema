package cinema.application.common.interfaces;

import java.util.List;
import java.util.Optional;
import java.time.Instant;
import cinema.domain.movie.Movie;
public interface MovieRepository {
    Optional<Movie> findById(String id);
    List<Movie> findAll();
    void save(Movie movie);
    void deleteById(String id);
}

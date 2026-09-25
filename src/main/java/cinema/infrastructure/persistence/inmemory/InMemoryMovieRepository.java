package cinema.infrastructure.persistence.inmemory;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import cinema.application.common.exceptions.ConflictException;
import cinema.application.common.interfaces.MovieRepository;
import cinema.domain.movie.Movie;

public final class InMemoryMovieRepository implements MovieRepository {

    private final Map<String, Movie> movies = new HashMap<>();

    @Override
    public synchronized Optional<Movie> findById(String id) {
        return Optional.ofNullable(movies.get(id));
    }

    @Override
    public synchronized List<Movie> findAll() {
        return movies.values().stream().sorted(Comparator.comparing(Movie::getId)).toList();
    }

    @Override
    public synchronized void save(Movie movie) {
        if (movies.containsKey(movie.getId())) {
            throw new ConflictException("Movie ID already exists: " + movie.getId());
        }
        movies.put(movie.getId(), movie);
    }

    @Override
    public synchronized void deleteById(String id) {
        movies.remove(id);
    }
}

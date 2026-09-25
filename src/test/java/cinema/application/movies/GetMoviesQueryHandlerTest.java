package cinema.application.movies;

import cinema.application.common.exceptions.ValidationException;
import cinema.application.common.interfaces.MovieRepository;
import cinema.application.common.models.MovieView;
import cinema.application.movies.queries.GetMoviesQuery;
import cinema.application.movies.queries.GetMoviesQueryHandler;
import cinema.domain.movie.Movie;
import cinema.domain.movie.MovieStatus;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GetMoviesQueryHandlerTest {
    private final FakeMovies movies = new FakeMovies();
    private final GetMoviesQueryHandler handler = new GetMoviesQueryHandler(movies);

    @Test
    void returnsAllMoviesMappedToViews() {
        movies.data = List.of(
                new Movie("movie-1", "Interstellar", "Space exploration", 169, "PG-13", MovieStatus.SHOWING),
                new Movie("movie-2", "Dune", "Arrakis", 155, "PG-13", MovieStatus.COMING_SOON));
        var result = handler.handle(new GetMoviesQuery());
        assertEquals(List.of(
                new MovieView("movie-1", "Interstellar", "Space exploration", 169, "PG-13", MovieStatus.SHOWING),
                new MovieView("movie-2", "Dune", "Arrakis", 155, "PG-13", MovieStatus.COMING_SOON)), result);
        assertEquals(2, result.size());
        assertEquals(1, movies.findAllCalls);
    }

    @Test
    void returnsEmptyListWhenRepositoryIsEmpty() {
        var result = handler.handle(new GetMoviesQuery());
        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(1, movies.findAllCalls);
    }

    @Test
    void rejectsNullQuery() {
        assertThrows(ValidationException.class, () -> handler.handle(null));
        assertEquals(0, movies.findAllCalls);
    }

    @Test
    void preservesUnsortedRepositoryOrderAndNullMetadata() {
        movies.data = List.of(
                new Movie("movie-2", "Zeta", null, 90, null, MovieStatus.ARCHIVED),
                new Movie("movie-1", "Alpha", "Story", 120, "P", MovieStatus.SHOWING));
        var result = handler.handle(new GetMoviesQuery());
        assertEquals(List.of(
                new MovieView("movie-2", "Zeta", null, 90, null, MovieStatus.ARCHIVED),
                new MovieView("movie-1", "Alpha", "Story", 120, "P", MovieStatus.SHOWING)), result);
        assertEquals(1, movies.findAllCalls);
    }

    private static final class FakeMovies implements MovieRepository {
        private List<Movie> data = List.of();
        private int findAllCalls;

        @Override
        public List<Movie> findAll() {
            findAllCalls++;
            return data;
        }

        @Override
        public Optional<Movie> findById(String id) { throw new AssertionError("GetMovies must not look up individual movies"); }

        @Override
        public void save(Movie movie) { throw new AssertionError("GetMovies must not save movies"); }

        @Override
        public void deleteById(String id) { throw new AssertionError("GetMovies must not delete movies"); }
    }
}

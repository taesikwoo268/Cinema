package cinema.application.movies;

import cinema.application.common.exceptions.NotFoundException;
import cinema.application.common.exceptions.ValidationException;
import cinema.application.common.interfaces.MovieRepository;
import cinema.application.movies.queries.GetMovieQuery;
import cinema.application.movies.queries.GetMovieQueryHandler;
import cinema.domain.movie.Movie;
import cinema.domain.movie.MovieStatus;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GetMovieQueryHandlerTest {
    private final FakeMovies movies = new FakeMovies();
    private final GetMovieQueryHandler handler = new GetMovieQueryHandler(movies);

    @Test
    void returnsMovieView() {
        movies.data.put("movie-1", new Movie("movie-1", "Interstellar", "Space exploration",
                169, "PG-13", MovieStatus.SHOWING));
        var view = handler.handle(new GetMovieQuery("movie-1"));
        assertEquals("movie-1", view.id());
        assertEquals("Interstellar", view.title());
        assertEquals("Space exploration", view.description());
        assertEquals(169, view.durationMinutes());
        assertEquals("PG-13", view.ageRating());
        assertEquals(MovieStatus.SHOWING, view.status());
        assertEquals(List.of("movie-1"), movies.lookups);
    }

    private void assertInvalid(GetMovieQuery query) {
        assertThrows(ValidationException.class, () -> handler.handle(query));
        assertTrue(movies.lookups.isEmpty());
    }

    @Test void rejectsNullQuery() { assertInvalid(null); }
    @Test void rejectsNullMovieId() { assertInvalid(new GetMovieQuery(null)); }
    @Test void rejectsEmptyMovieId() { assertInvalid(new GetMovieQuery("")); }
    @Test void rejectsBlankMovieId() { assertInvalid(new GetMovieQuery(" \t\n")); }

    @Test
    void missingMovieThrowsNotFound() {
        assertThrows(NotFoundException.class, () -> handler.handle(new GetMovieQuery("missing")));
        assertEquals(List.of("missing"), movies.lookups);
    }

    @Test
    void preservesExactIdAndNullMetadata() {
        movies.data.put(" movie-1 ", new Movie(" movie-1 ", " Movie ", null, 90, null, MovieStatus.ARCHIVED));
        var view = handler.handle(new GetMovieQuery(" movie-1 "));
        assertEquals(" movie-1 ", view.id());
        assertEquals(" Movie ", view.title());
        assertNull(view.description());
        assertNull(view.ageRating());
        assertEquals(MovieStatus.ARCHIVED, view.status());
        assertEquals(List.of(" movie-1 "), movies.lookups);
    }

    private static final class FakeMovies implements MovieRepository {
        private final Map<String, Movie> data = new HashMap<>();
        private final List<String> lookups = new ArrayList<>();

        @Override
        public Optional<Movie> findById(String id) {
            lookups.add(id);
            return Optional.ofNullable(data.get(id));
        }

        @Override
        public List<Movie> findAll() { throw new AssertionError("GetMovie must not list movies"); }

        @Override
        public void save(Movie movie) { throw new AssertionError("GetMovie must not save movies"); }

        @Override
        public void deleteById(String id) { throw new AssertionError("GetMovie must not delete movies"); }
    }
}

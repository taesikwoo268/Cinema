package cinema.application.movies;

import cinema.application.common.exceptions.NotFoundException;
import cinema.application.common.exceptions.ValidationException;
import cinema.application.common.interfaces.MovieRepository;
import cinema.application.movies.commands.DeleteMovieCommand;
import cinema.application.movies.commands.DeleteMovieCommandHandler;
import cinema.domain.movie.Movie;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DeleteMovieCommandHandlerTest {
    private final FakeMovies movies = new FakeMovies();
    private final DeleteMovieCommandHandler handler = new DeleteMovieCommandHandler(movies);

    @Test
    void deletesExistingMovie() {
        movies.data.put("movie-1", new Movie("movie-1", "Movie", null, 120, null, null));
        assertNull(handler.handle(new DeleteMovieCommand("movie-1")));
        assertEquals(List.of("find:movie-1", "delete:movie-1"), movies.calls);
        assertFalse(movies.data.containsKey("movie-1"));
    }

    private void assertInvalid(DeleteMovieCommand command) {
        movies.data.put("movie-1", new Movie("movie-1", "Movie", null, 120, null, null));
        assertThrows(ValidationException.class, () -> handler.handle(command));
        assertTrue(movies.calls.isEmpty());
        assertTrue(movies.data.containsKey("movie-1"));
    }

    @Test void rejectsNullCommand() { assertInvalid(null); }
    @Test void rejectsNullMovieId() { assertInvalid(new DeleteMovieCommand(null)); }
    @Test void rejectsEmptyMovieId() { assertInvalid(new DeleteMovieCommand("")); }
    @Test void rejectsBlankMovieId() { assertInvalid(new DeleteMovieCommand(" \t\n")); }

    @Test
    void missingMovieThrowsNotFound() {
        assertThrows(NotFoundException.class, () -> handler.handle(new DeleteMovieCommand("missing")));
        assertEquals(List.of("find:missing"), movies.calls);
        assertTrue(movies.data.isEmpty());
    }

    @Test
    void preservesExactIdForLookupAndDelete() {
        movies.data.put(" movie-1 ", new Movie(" movie-1 ", "Movie", null, 120, null, null));
        assertNull(handler.handle(new DeleteMovieCommand(" movie-1 ")));
        assertEquals(List.of("find: movie-1 ", "delete: movie-1 "), movies.calls);
        assertTrue(movies.data.isEmpty());
    }

    private static final class FakeMovies implements MovieRepository {
        private final Map<String, Movie> data = new HashMap<>();
        private final List<String> calls = new ArrayList<>();

        @Override
        public Optional<Movie> findById(String id) {
            calls.add("find:" + id);
            return Optional.ofNullable(data.get(id));
        }

        @Override
        public List<Movie> findAll() {
            throw new AssertionError("Delete must not list movies");
        }

        @Override
        public void save(Movie movie) {
            throw new AssertionError("Delete must not save movies");
        }

        @Override
        public void deleteById(String id) {
            calls.add("delete:" + id);
            data.remove(id);
        }
    }
}

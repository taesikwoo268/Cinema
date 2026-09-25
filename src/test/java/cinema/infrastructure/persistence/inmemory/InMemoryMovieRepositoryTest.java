package cinema.infrastructure.persistence.inmemory;

import org.junit.jupiter.api.Test;

import cinema.application.common.exceptions.ConflictException;
import cinema.domain.movie.Movie;
import cinema.domain.movie.MovieStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryMovieRepositoryTest {

    private final InMemoryMovieRepository repository = new InMemoryMovieRepository();

    private Movie movie(String id, String title) {
        return new Movie(id, title, "Space exploration", 169, "PG-13", MovieStatus.SHOWING);
    }

    @Test
    void savesAndFindsMovieById() {
        Movie movie = movie("movie-1", "Interstellar");
        repository.save(movie);

        Movie found = repository.findById("movie-1").orElseThrow();
        assertSame(movie, found);
        assertEquals("movie-1", found.getId());
        assertEquals("Interstellar", found.getTitle());
        assertEquals("Space exploration", found.getDescription());
        assertEquals(169, found.getDurationMinutes());
        assertEquals("PG-13", found.getAgeRating());
        assertEquals(MovieStatus.SHOWING, found.getStatus());
    }

    @Test
    void missingIdReturnsEmpty() {
        assertTrue(repository.findById("missing").isEmpty());
    }

    @Test
    void findAllReturnsAllMovies() {
        Movie second = movie("movie-2", "Alpha");
        Movie first = movie("movie-1", "Zeta");
        repository.save(second);
        repository.save(first);

        List<Movie> result = repository.findAll();
        assertEquals(2, result.size());
        assertEquals(List.of(first, second), result);
    }

    @Test
    void emptyRepositoryReturnsEmptyList() {
        List<Movie> result = repository.findAll();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void deleteRemovesExistingMovie() {
        repository.save(movie("movie-1", "Interstellar"));
        Movie remaining = movie("movie-2", "Dune");
        repository.save(remaining);

        repository.deleteById("movie-1");

        assertTrue(repository.findById("movie-1").isEmpty());
        assertEquals(List.of(remaining), repository.findAll());
    }

    @Test
    void duplicateIdFollowsM1Convention() {
        Movie original = movie("movie-1", "Interstellar");
        repository.save(original);

        ConflictException exception = assertThrows(ConflictException.class,
                () -> repository.save(movie("movie-1", "Dune")));

        assertEquals("Movie ID already exists: movie-1", exception.getMessage());
        assertSame(original, repository.findById("movie-1").orElseThrow());
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void findAllReturnsSnapshot() {
        Movie first = movie("movie-1", "Interstellar");
        Movie second = movie("movie-2", "Dune");
        repository.save(first);
        List<Movie> result = repository.findAll();

        assertThrows(UnsupportedOperationException.class, () -> result.add(second));
        assertThrows(UnsupportedOperationException.class, result::clear);
        assertSame(first, repository.findById("movie-1").orElseThrow());
        repository.save(second);
        repository.deleteById("movie-1");

        assertEquals(List.of(first), result);
        assertEquals(List.of(second), repository.findAll());
    }

    @Test
    void differentIdsMayShareTitle() {
        Movie first = movie("movie-1", "Interstellar");
        Movie second = movie("movie-2", "Interstellar");
        repository.save(first);
        repository.save(second);

        assertEquals(List.of(first, second), repository.findAll());
    }

    @Test
    void deletingMissingIdIsNoOp() {
        Movie existing = movie("movie-1", "Interstellar");
        repository.save(existing);

        assertDoesNotThrow(() -> repository.deleteById("missing"));
        assertEquals(List.of(existing), repository.findAll());
    }
}

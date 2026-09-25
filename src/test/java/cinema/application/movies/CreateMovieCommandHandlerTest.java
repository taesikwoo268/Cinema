package cinema.application.movies;

import cinema.application.common.exceptions.ValidationException;
import cinema.application.common.interfaces.MovieRepository;
import cinema.application.movies.commands.CreateMovieCommand;
import cinema.application.movies.commands.CreateMovieCommandHandler;
import cinema.domain.movie.Movie;
import cinema.domain.movie.MovieStatus;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CreateMovieCommandHandlerTest {
    private final MovieRepository movies = mock(MovieRepository.class);
    private int generatedIds;
    private final Supplier<String> ids = () -> { generatedIds++; return "movie-1"; };
    private final CreateMovieCommandHandler handler = new CreateMovieCommandHandler(movies, ids);

    @Test
    void createsMovieAndReturnsGeneratedId() {
        assertEquals("movie-1", handler.handle(new CreateMovieCommand(" Title ", " Description ", 120, " T16 ")));
        var saved = ArgumentCaptor.forClass(Movie.class);
        verify(movies, times(1)).save(saved.capture());
        verifyNoMoreInteractions(movies);
        Movie movie = saved.getValue();
        assertEquals("movie-1", movie.getId());
        assertEquals(" Title ", movie.getTitle());
        assertEquals(" Description ", movie.getDescription());
        assertEquals(120, movie.getDurationMinutes());
        assertEquals(" T16 ", movie.getAgeRating());
        assertEquals(MovieStatus.COMING_SOON, movie.getStatus());
        assertEquals(1, generatedIds);
    }

    private void assertInvalid(CreateMovieCommand command) {
        assertThrows(ValidationException.class, () -> handler.handle(command));
        verifyNoInteractions(movies);
        assertEquals(0, generatedIds);
    }

    @Test void rejectsNullCommand() { assertInvalid(null); }
    @Test void rejectsNullTitle() { assertInvalid(new CreateMovieCommand(null, null, 120, null)); }
    @Test void rejectsEmptyTitle() { assertInvalid(new CreateMovieCommand("", null, 120, null)); }
    @Test void rejectsBlankTitle() { assertInvalid(new CreateMovieCommand(" \t\n", null, 120, null)); }
    @Test void rejectsZeroDuration() { assertInvalid(new CreateMovieCommand("Title", null, 0, null)); }
    @Test void rejectsNegativeDuration() { assertInvalid(new CreateMovieCommand("Title", null, -1, null)); }

    @Test
    void preservesNullMetadata() {
        handler.handle(new CreateMovieCommand("Title", null, 120, null));
        var saved = ArgumentCaptor.forClass(Movie.class);
        verify(movies).save(saved.capture());
        assertNull(saved.getValue().getDescription());
        assertNull(saved.getValue().getAgeRating());
        assertEquals(MovieStatus.COMING_SOON, saved.getValue().getStatus());
    }

    @Test
    void domainFailureBecomesValidationWithoutSave() {
        var invalidIdHandler = new CreateMovieCommandHandler(movies, () -> " ");
        assertThrows(ValidationException.class,
                () -> invalidIdHandler.handle(new CreateMovieCommand("Title", null, 120, null)));
        verifyNoInteractions(movies);
    }

    @Test
    void repositoryFailurePropagatesUnchanged() {
        var failure = new IllegalStateException("Storage unavailable");
        doThrow(failure).when(movies).save(any());
        assertSame(failure, assertThrows(IllegalStateException.class,
                () -> handler.handle(new CreateMovieCommand("Title", null, 120, null))));
        verify(movies, times(1)).save(any());
    }
}

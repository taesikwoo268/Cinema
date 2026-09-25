package cinema.application.screenings.commands;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InOrder;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import cinema.application.common.exceptions.ConflictException;
import cinema.application.common.exceptions.NotFoundException;
import cinema.application.common.exceptions.ValidationException;
import cinema.application.common.interfaces.HallRepository;
import cinema.application.common.interfaces.MovieRepository;
import cinema.application.common.interfaces.ScreeningRepository;
import cinema.domain.cinema.Hall;
import cinema.domain.common.Money;
import cinema.domain.movie.Movie;
import cinema.domain.movie.MovieStatus;
import cinema.domain.screening.Screening;
import cinema.domain.screening.ScreeningStatus;

class CreateScreeningCommandHandlerTest {

    private static final Instant START = Instant.parse("2026-10-01T10:00:00Z");
    private static final Instant END = Instant.parse("2026-10-01T12:00:00Z");
    private static final BigDecimal BASE_PRICE = new BigDecimal("100000.00");

    private final ScreeningRepository screenings = mock(ScreeningRepository.class);
    private final MovieRepository movies = mock(MovieRepository.class);
    private final HallRepository halls = mock(HallRepository.class);
    private final AtomicInteger generatedIds = new AtomicInteger();
    private final CreateScreeningCommandHandler handler = new CreateScreeningCommandHandler(
            screenings, movies, halls, () -> {
                generatedIds.incrementAndGet();
                return "screening-1";
            });

    private CreateScreeningCommand command() {
        return new CreateScreeningCommand("movie-1", "hall-1", START, END, BASE_PRICE, "VND");
    }

    private void existingParents() {
        when(movies.findById("movie-1")).thenReturn(Optional.of(
                new Movie("movie-1", "Movie", "Description", 120, "P", MovieStatus.COMING_SOON)));
        when(halls.findById("hall-1")).thenReturn(Optional.of(new Hall("hall-1", "cinema-1", "Hall")));
    }

    @Test
    void createsScreeningAndReturnsGeneratedId() {
        existingParents();

        assertEquals("screening-1", handler.handle(command()));

        var saved = ArgumentCaptor.forClass(Screening.class);
        verify(screenings).save(saved.capture());
        Screening screening = saved.getValue();
        assertAll(
                () -> assertEquals("screening-1", screening.getId()),
                () -> assertEquals("movie-1", screening.getMovieId()),
                () -> assertEquals("hall-1", screening.getHallId()),
                () -> assertEquals(START, screening.getStartTime()),
                () -> assertEquals(END, screening.getEndTime()),
                () -> assertEquals(new Money(BASE_PRICE, Currency.getInstance("VND")), screening.getBasePrice()),
                () -> assertEquals(ScreeningStatus.SCHEDULED, screening.getStatus()),
                () -> assertEquals(1, generatedIds.get()));
        InOrder order = inOrder(movies, halls, screenings);
        order.verify(movies).findById("movie-1");
        order.verify(halls).findById("hall-1");
        order.verify(screenings).hasOverlap("hall-1", START, END);
        order.verify(screenings).save(any(Screening.class));
    }

    private void assertInvalid(CreateScreeningCommand input) {
        var exception = assertThrows(ValidationException.class, () -> handler.handle(input));
        assertEquals(ValidationException.class, exception.getClass());
        verifyNoInteractions(movies, halls, screenings);
        assertEquals(0, generatedIds.get());
    }

    @Test
    void rejectsNullCommand() {
        assertInvalid(null);
    }

    @Test
    void rejectsBlankMovieId() {
        assertInvalid(new CreateScreeningCommand(" ", "hall-1", START, END, BASE_PRICE, "VND"));
    }

    @Test
    void rejectsBlankHallId() {
        assertInvalid(new CreateScreeningCommand("movie-1", " ", START, END, BASE_PRICE, "VND"));
    }

    @Test
    void rejectsNullStartTime() {
        assertInvalid(new CreateScreeningCommand("movie-1", "hall-1", null, END, BASE_PRICE, "VND"));
    }

    @Test
    void rejectsNullEndTime() {
        assertInvalid(new CreateScreeningCommand("movie-1", "hall-1", START, null, BASE_PRICE, "VND"));
    }

    @Test
    void rejectsEqualTimes() {
        assertInvalid(new CreateScreeningCommand("movie-1", "hall-1", START, START, BASE_PRICE, "VND"));
    }

    @Test
    void rejectsReversedTimes() {
        assertInvalid(new CreateScreeningCommand("movie-1", "hall-1", END, START, BASE_PRICE, "VND"));
    }

    @Test
    void rejectsNegativeBasePrice() {
        assertInvalid(new CreateScreeningCommand("movie-1", "hall-1", START, END, new BigDecimal("-1"), "VND"));
    }

    @Test
    void rejectsInvalidCurrency() {
        assertInvalid(new CreateScreeningCommand("movie-1", "hall-1", START, END, BASE_PRICE, "invalid"));
    }

    @Test
    void missingMovieStopsBeforeHallLookup() {
        when(movies.findById("movie-1")).thenReturn(Optional.empty());

        var exception = assertThrows(NotFoundException.class, () -> handler.handle(command()));
        assertEquals(NotFoundException.class, exception.getClass());

        verify(movies).findById("movie-1");
        verifyNoInteractions(halls, screenings);
        assertEquals(0, generatedIds.get());
    }

    @Test
    void missingHallStopsBeforeOverlap() {
        when(movies.findById("movie-1")).thenReturn(Optional.of(
                new Movie("movie-1", "Movie", "Description", 120, "P", MovieStatus.COMING_SOON)));
        when(halls.findById("hall-1")).thenReturn(Optional.empty());

        var exception = assertThrows(NotFoundException.class, () -> handler.handle(command()));
        assertEquals(NotFoundException.class, exception.getClass());

        verify(movies).findById("movie-1");
        verify(halls).findById("hall-1");
        verifyNoInteractions(screenings);
        assertEquals(0, generatedIds.get());
    }

    @Test
    void overlapStopsBeforeIdGenerationAndSave() {
        existingParents();
        when(screenings.hasOverlap("hall-1", START, END)).thenReturn(true);

        var exception = assertThrows(ConflictException.class, () -> handler.handle(command()));
        assertEquals(ConflictException.class, exception.getClass());

        verify(screenings).hasOverlap("hall-1", START, END);
        verify(screenings, never()).save(any());
        assertEquals(0, generatedIds.get());
    }
}

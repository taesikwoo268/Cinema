package cinema.application.screenings.queries;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import cinema.application.common.exceptions.NotFoundException;
import cinema.application.common.exceptions.ValidationException;
import cinema.application.common.interfaces.ScreeningRepository;
import cinema.application.common.models.ScreeningView;
import cinema.domain.common.Money;
import cinema.domain.screening.Screening;
import cinema.domain.screening.ScreeningStatus;

class GetScreeningQueryHandlerTest {

    private static final Instant START = Instant.parse("2026-10-01T10:00:00Z");
    private static final Instant END = Instant.parse("2026-10-01T12:00:00Z");
    private static final Money PRICE = new Money(new BigDecimal("100000.00"), Currency.getInstance("VND"));

    private final ScreeningRepository screenings = mock(ScreeningRepository.class);
    private final GetScreeningQueryHandler handler = new GetScreeningQueryHandler(screenings);

    @Test
    void returnsAllScreeningFieldsMappedFromDomain() {
        Screening screening = new Screening("screening-1", "movie-1", "hall-1", START, END, PRICE,
                ScreeningStatus.CANCELLED);
        when(screenings.findById("screening-1")).thenReturn(Optional.of(screening));

        ScreeningView view = handler.handle(new GetScreeningQuery("screening-1"));

        assertAll(
                () -> assertEquals("screening-1", view.id()),
                () -> assertEquals("movie-1", view.movieId()),
                () -> assertEquals("hall-1", view.hallId()),
                () -> assertEquals(START, view.startTime()),
                () -> assertEquals(END, view.endTime()),
                () -> assertEquals(new BigDecimal("100000.00"), view.basePrice()),
                () -> assertEquals("VND", view.currency()),
                () -> assertEquals("CANCELLED", view.status()));
        verify(screenings).findById("screening-1");
    }

    private void assertInvalid(GetScreeningQuery query) {
        var exception = assertThrows(ValidationException.class, () -> handler.handle(query));
        assertEquals(ValidationException.class, exception.getClass());
        verifyNoInteractions(screenings);
    }

    @Test
    void rejectsNullQueryBeforeRepositoryLookup() {
        assertInvalid(null);
    }

    @Test
    void rejectsNullScreeningIdBeforeRepositoryLookup() {
        assertInvalid(new GetScreeningQuery(null));
    }

    @Test
    void rejectsBlankScreeningIdBeforeRepositoryLookup() {
        assertInvalid(new GetScreeningQuery(" \t\n"));
    }

    @Test
    void missingScreeningThrowsNotFound() {
        when(screenings.findById("missing")).thenReturn(Optional.empty());

        var exception = assertThrows(NotFoundException.class,
                () -> handler.handle(new GetScreeningQuery("missing")));
        assertEquals(NotFoundException.class, exception.getClass());
        verify(screenings).findById("missing");
    }
}

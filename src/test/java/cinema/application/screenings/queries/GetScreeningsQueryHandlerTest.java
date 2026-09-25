package cinema.application.screenings.queries;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import cinema.application.common.exceptions.ValidationException;
import cinema.application.common.interfaces.ScreeningRepository;
import cinema.application.common.models.ScreeningView;
import cinema.domain.common.Money;
import cinema.domain.screening.Screening;
import cinema.domain.screening.ScreeningStatus;

class GetScreeningsQueryHandlerTest {

    private static final Instant FIRST_START = Instant.parse("2026-10-01T10:00:00Z");
    private static final Instant FIRST_END = Instant.parse("2026-10-01T12:00:00Z");
    private static final Instant SECOND_START = Instant.parse("2026-10-02T18:00:00Z");
    private static final Instant SECOND_END = Instant.parse("2026-10-02T20:30:00Z");

    private final ScreeningRepository screenings = mock(ScreeningRepository.class);
    private final GetScreeningsQueryHandler handler = new GetScreeningsQueryHandler(screenings);

    @Test
    void returnsAllScreeningsInRepositoryOrderWithAllFieldsMapped() {
        Screening first = new Screening("screening-1", "movie-1", "hall-1", FIRST_START, FIRST_END,
                new Money(new BigDecimal("100000.00"), Currency.getInstance("VND")),
                ScreeningStatus.SCHEDULED);
        Screening second = new Screening("screening-2", "movie-1", "hall-2", SECOND_START, SECOND_END,
                new Money(new BigDecimal("12.50"), Currency.getInstance("USD")),
                ScreeningStatus.CANCELLED);
        when(screenings.findByMovieId("movie-1")).thenReturn(List.of(second, first));

        List<ScreeningView> views = handler.handle(new GetScreeningsQuery("movie-1"));

        assertEquals(2, views.size());
        assertView(views.get(0), second, "USD", "CANCELLED");
        assertView(views.get(1), first, "VND", "SCHEDULED");
        verify(screenings).findByMovieId("movie-1");
    }

    private static void assertView(ScreeningView view, Screening screening, String currency, String status) {
        assertEquals(screening.getId(), view.id());
        assertEquals(screening.getMovieId(), view.movieId());
        assertEquals(screening.getHallId(), view.hallId());
        assertEquals(screening.getStartTime(), view.startTime());
        assertEquals(screening.getEndTime(), view.endTime());
        assertEquals(screening.getBasePrice().amount(), view.basePrice());
        assertEquals(currency, view.currency());
        assertEquals(status, view.status());
    }

    @Test
    void returnsEmptyListWhenRepositoryReturnsNoScreenings() {
        when(screenings.findByMovieId("movie-1")).thenReturn(List.of());

        List<ScreeningView> views = handler.handle(new GetScreeningsQuery("movie-1"));

        assertFalse(views.iterator().hasNext());
        verify(screenings).findByMovieId("movie-1");
    }

    private void assertInvalid(GetScreeningsQuery query) {
        var exception = assertThrows(ValidationException.class, () -> handler.handle(query));
        assertEquals(ValidationException.class, exception.getClass());
        verifyNoInteractions(screenings);
    }

    @Test
    void rejectsNullQueryBeforeRepositoryLookup() {
        assertInvalid(null);
    }

    @Test
    void rejectsNullMovieIdBeforeRepositoryLookup() {
        assertInvalid(new GetScreeningsQuery(null));
    }

    @Test
    void rejectsBlankMovieIdBeforeRepositoryLookup() {
        assertInvalid(new GetScreeningsQuery(" \t\n"));
    }
}

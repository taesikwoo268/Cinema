package cinema.infrastructure.persistence.inmemory;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import cinema.domain.common.Money;
import cinema.domain.screening.Screening;
import cinema.domain.screening.ScreeningStatus;

class InMemoryScreeningRepositoryTest {

    private static final Instant TEN = Instant.parse("2026-10-01T10:00:00Z");
    private static final Instant TWELVE = Instant.parse("2026-10-01T12:00:00Z");
    private static final Money PRICE = new Money(new BigDecimal("100000.00"), Currency.getInstance("VND"));

    private final InMemoryScreeningRepository repository = new InMemoryScreeningRepository();

    private Screening screening(String id, String movieId, String hallId, Instant start, Instant end) {
        return new Screening(id, movieId, hallId, start, end, PRICE);
    }

    @Test
    void savesAndFindsScreeningById() {
        Screening screening = screening("s1", "m1", "h1", TEN, TWELVE);

        repository.save(screening);

        assertSame(screening, repository.findById("s1").orElseThrow());
    }

    @Test
    void missingIdReturnsEmpty() {
        assertTrue(repository.findById("missing").isEmpty());
    }

    @Test
    void saveExistingScreeningUpdatesItForCancellation() {
        Screening screening = screening("s1", "m1", "h1", TEN, TWELVE);
        repository.save(screening);

        screening.cancel();
        repository.save(screening);

        assertSame(screening, repository.findById("s1").orElseThrow());
        assertEquals(ScreeningStatus.CANCELLED, repository.findById("s1").orElseThrow().getStatus());
    }

    @Test
    void findByMovieIdPreservesInsertionOrderAndFiltersOtherMovies() {
        Screening second = screening("s2", "m1", "h1", TWELVE, Instant.parse("2026-10-01T14:00:00Z"));
        Screening otherMovie = screening("s3", "m2", "h1", TWELVE, Instant.parse("2026-10-01T14:00:00Z"));
        Screening first = screening("s1", "m1", "h2", TEN, TWELVE);
        repository.save(second);
        repository.save(otherMovie);
        repository.save(first);

        assertEquals(List.of(second, first), repository.findByMovieId("m1"));
    }

    @Test
    void findByMovieIdReturnsEmptyWhenMovieHasNoScreenings() {
        assertTrue(repository.findByMovieId("missing").isEmpty());
    }

    @Test
    void findByMovieIdReturnsSnapshot() {
        Screening first = screening("s1", "m1", "h1", TEN, TWELVE);
        repository.save(first);
        List<Screening> result = repository.findByMovieId("m1");

        var addException = assertThrows(UnsupportedOperationException.class, () -> result.add(
                screening("s2", "m1", "h1", TWELVE, Instant.parse("2026-10-01T14:00:00Z"))));
        var clearException = assertThrows(UnsupportedOperationException.class, result::clear);
        assertEquals(UnsupportedOperationException.class, addException.getClass());
        assertEquals(UnsupportedOperationException.class, clearException.getClass());
        repository.save(screening("s2", "m1", "h1", TWELVE, Instant.parse("2026-10-01T14:00:00Z")));

        assertEquals(List.of(first), result);
        assertEquals(2, repository.findByMovieId("m1").size());
    }

    @Test
    void exactSameIntervalOverlaps() {
        repository.save(screening("existing", "m1", "h1", TEN, TWELVE));

        assertTrue(repository.hasOverlap("h1", TEN, TWELVE));
    }

    @Test
    void intervalInsideExistingOverlaps() {
        repository.save(screening("existing", "m1", "h1", TEN, TWELVE));

        assertTrue(repository.hasOverlap("h1", Instant.parse("2026-10-01T10:30:00Z"),
                Instant.parse("2026-10-01T11:30:00Z")));
    }

    @Test
    void intervalContainingExistingOverlaps() {
        repository.save(screening("existing", "m1", "h1", TEN, TWELVE));

        assertTrue(repository.hasOverlap("h1", Instant.parse("2026-10-01T09:00:00Z"),
                Instant.parse("2026-10-01T13:00:00Z")));
    }

    @Test
    void leftAndRightPartialOverlaps() {
        repository.save(screening("existing", "m1", "h1", TEN, TWELVE));

        assertTrue(repository.hasOverlap("h1", Instant.parse("2026-10-01T09:00:00Z"),
                Instant.parse("2026-10-01T10:01:00Z")));
        assertTrue(repository.hasOverlap("h1", Instant.parse("2026-10-01T11:59:00Z"),
                Instant.parse("2026-10-01T13:00:00Z")));
    }

    @Test
    void touchingBoundariesDoNotOverlap() {
        repository.save(screening("existing", "m1", "h1", TEN, TWELVE));

        assertFalse(repository.hasOverlap("h1", Instant.parse("2026-10-01T08:00:00Z"), TEN));
        assertFalse(repository.hasOverlap("h1", TWELVE, Instant.parse("2026-10-01T14:00:00Z")));
    }

    @Test
    void completelyBeforeOrAfterDoesNotOverlap() {
        repository.save(screening("existing", "m1", "h1", TEN, TWELVE));

        assertFalse(repository.hasOverlap("h1", Instant.parse("2026-10-01T07:00:00Z"),
                Instant.parse("2026-10-01T08:00:00Z")));
        assertFalse(repository.hasOverlap("h1", Instant.parse("2026-10-01T14:00:00Z"),
                Instant.parse("2026-10-01T15:00:00Z")));
    }

    @Test
    void sameTimeInDifferentHallDoesNotOverlap() {
        repository.save(screening("existing", "m1", "h1", TEN, TWELVE));

        assertFalse(repository.hasOverlap("h2", TEN, TWELVE));
    }
}

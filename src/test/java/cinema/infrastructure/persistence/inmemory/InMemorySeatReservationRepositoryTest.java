package cinema.infrastructure.persistence.inmemory;

import cinema.application.common.exceptions.ConflictException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class InMemorySeatReservationRepositoryTest {
    private final InMemorySeatReservationRepository repository = new InMemorySeatReservationRepository();

    @Test
    void initiallyUnbooked() {
        assertFalse(repository.isBooked("s1", "a1"));
    }

    @Test
    void firstBookingStoresPair() {
        repository.markBooked("s1", "a1", "b1");
        assertTrue(repository.isBooked("s1", "a1"));
        assertFalse(repository.isBooked("s1", "missing"));
        assertFalse(repository.isBooked("missing", "a1"));
    }

    @Test
    void sameBookingIsIdempotent() {
        repository.markBooked("s1", "a1", "b1");
        assertDoesNotThrow(() -> repository.markBooked("s1", "a1", "b1"));
        assertTrue(repository.isBooked("s1", "a1"));
    }

    @Test
    void differentBookingConflictsAndPreservesOriginal() {
        repository.markBooked("s1", "a1", "b1");
        assertThrows(ConflictException.class, () -> repository.markBooked("s1", "a1", "b2"));
        assertTrue(repository.isBooked("s1", "a1"));
        assertDoesNotThrow(() -> repository.markBooked("s1", "a1", "b1"));
        assertThrows(ConflictException.class, () -> repository.markBooked("s1", "a1", "b2"));
    }

    @Test
    void sameSeatInDifferentScreeningsIsIndependent() {
        repository.markBooked("s1", "a1", "b1");
        assertFalse(repository.isBooked("s2", "a1"));
        assertDoesNotThrow(() -> repository.markBooked("s2", "a1", "b2"));
        assertTrue(repository.isBooked("s1", "a1"));
        assertTrue(repository.isBooked("s2", "a1"));
    }

    @Test
    void differentSeatsInSameScreeningAreIndependent() {
        repository.markBooked("s1", "a1", "b1");
        assertFalse(repository.isBooked("s1", "a2"));
        assertDoesNotThrow(() -> repository.markBooked("s1", "a2", "b2"));
        assertTrue(repository.isBooked("s1", "a1"));
        assertTrue(repository.isBooked("s1", "a2"));
    }

    @Test
    void keyPreservesPairBoundaries() {
        repository.markBooked("a:b", "c", "b1");
        assertFalse(repository.isBooked("a", "b:c"));
        repository.markBooked("a", "b:c", "b2");
        assertDoesNotThrow(() -> repository.markBooked("a:b", "c", "b1"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t\n"})
    void rejectsInvalidIdsInAllOperations(String invalid) {
        repository.markBooked("s1", "a1", "b1");
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> repository.isBooked(invalid, "a1")),
                () -> assertThrows(IllegalArgumentException.class, () -> repository.isBooked("s1", invalid)),
                () -> assertThrows(IllegalArgumentException.class, () -> repository.markBooked(invalid, "a1", "b1")),
                () -> assertThrows(IllegalArgumentException.class, () -> repository.markBooked("s1", invalid, "b1")),
                () -> assertThrows(IllegalArgumentException.class, () -> repository.markBooked("s1", "a1", invalid)),
                () -> assertThrows(IllegalArgumentException.class, () -> repository.markBooked("s2", "a2", invalid)));
        assertFalse(repository.isBooked("s2", "a2"));
        assertDoesNotThrow(() -> repository.markBooked("s1", "a1", "b1"));
    }

    @Test
    void competingBookingsHaveExactlyOneWinner() throws Exception {
        CountDownLatch start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> bookAfter(start, "b1"));
            var second = executor.submit(() -> bookAfter(start, "b2"));
            start.countDown();
            boolean firstWon = first.get(5, TimeUnit.SECONDS);
            boolean secondWon = second.get(5, TimeUnit.SECONDS);
            assertNotEquals(firstWon, secondWon);
            assertTrue(repository.isBooked("s1", "a1"));
            String winner = firstWon ? "b1" : "b2";
            String loser = firstWon ? "b2" : "b1";
            assertDoesNotThrow(() -> repository.markBooked("s1", "a1", winner));
            assertThrows(ConflictException.class, () -> repository.markBooked("s1", "a1", loser));
        }
    }

    private boolean bookAfter(CountDownLatch start, String bookingId) throws InterruptedException {
        start.await();
        try {
            repository.markBooked("s1", "a1", bookingId);
            return true;
        } catch (ConflictException expected) {
            return false;
        }
    }
}

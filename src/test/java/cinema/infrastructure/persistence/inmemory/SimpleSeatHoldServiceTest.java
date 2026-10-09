package cinema.infrastructure.persistence.inmemory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class SimpleSeatHoldServiceTest {
    private final SimpleSeatHoldService service = new SimpleSeatHoldService();

    @Test
    void unheldSeatCanBeHeld() {
        assertFalse(service.isHeldBy("c1", "s1", "a1"));
        assertTrue(service.hold("c1", "s1", "a1"));
        assertTrue(service.isHeldBy("c1", "s1", "a1"));
    }

    @Test
    void sameOwnerHoldIsIdempotent() {
        assertTrue(service.hold("c1", "s1", "a1"));
        assertTrue(service.hold("c1", "s1", "a1"));
        assertTrue(service.isHeldBy("c1", "s1", "a1"));
        service.release("c1", "s1", "a1");
        assertFalse(service.isHeldBy("c1", "s1", "a1"));
    }

    @Test
    void differentOwnerCannotTakeHeldSeat() {
        assertTrue(service.hold("c1", "s1", "a1"));
        assertFalse(service.hold("c2", "s1", "a1"));
        assertTrue(service.isHeldBy("c1", "s1", "a1"));
        assertFalse(service.isHeldBy("c2", "s1", "a1"));
    }

    @Test
    void differentScreeningsWithSameSeatAreIndependent() {
        assertTrue(service.hold("c1", "s1", "a1"));
        assertTrue(service.hold("c2", "s2", "a1"));
        assertTrue(service.isHeldBy("c1", "s1", "a1"));
        assertTrue(service.isHeldBy("c2", "s2", "a1"));
        service.release("c1", "s1", "a1");
        assertTrue(service.isHeldBy("c2", "s2", "a1"));
    }

    @Test
    void differentSeatsInSameScreeningAreIndependent() {
        assertTrue(service.hold("c1", "s1", "a1"));
        assertTrue(service.hold("c2", "s1", "a2"));
        service.release("c1", "s1", "a1");
        assertTrue(service.isHeldBy("c2", "s1", "a2"));
    }

    @Test
    void keyPreservesPairBoundaries() {
        assertTrue(service.hold("c1", "a:b", "c"));
        assertTrue(service.hold("c2", "a", "b:c"));
        assertTrue(service.isHeldBy("c1", "a:b", "c"));
        assertTrue(service.isHeldBy("c2", "a", "b:c"));
    }

    @Test
    void ownerCanReleaseAndAnotherCustomerCanThenHold() {
        service.hold("c1", "s1", "a1");
        service.release("c1", "s1", "a1");
        assertFalse(service.isHeldBy("c1", "s1", "a1"));
        assertTrue(service.hold("c2", "s1", "a1"));
        assertTrue(service.isHeldBy("c2", "s1", "a1"));
    }

    @Test
    void nonOwnerCannotRelease() {
        service.hold("c1", "s1", "a1");
        service.release("c2", "s1", "a1");
        assertTrue(service.isHeldBy("c1", "s1", "a1"));
        assertFalse(service.hold("c2", "s1", "a1"));
    }

    @Test
    void releasingUnheldSeatIsNoOp() {
        service.hold("c1", "s1", "a1");
        assertDoesNotThrow(() -> service.release("c1", "s1", "missing"));
        assertFalse(service.isHeldBy("c1", "s1", "missing"));
        assertTrue(service.isHeldBy("c1", "s1", "a1"));
    }

    @Test
    void isHeldByRequiresExactOwnerAndPair() {
        service.hold("c1", "s1", "a1");
        assertTrue(service.isHeldBy("c1", "s1", "a1"));
        assertFalse(service.isHeldBy("c2", "s1", "a1"));
        assertFalse(service.isHeldBy("c1", "s2", "a1"));
        assertFalse(service.isHeldBy("c1", "s1", "a2"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t\n"})
    void allOperationsRejectInvalidArguments(String invalid) {
        service.hold("c1", "s1", "a1");
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> service.hold(invalid, "s1", "a1")),
                () -> assertThrows(IllegalArgumentException.class, () -> service.hold("c1", invalid, "a1")),
                () -> assertThrows(IllegalArgumentException.class, () -> service.hold("c1", "s1", invalid)),
                () -> assertThrows(IllegalArgumentException.class, () -> service.release(invalid, "s1", "a1")),
                () -> assertThrows(IllegalArgumentException.class, () -> service.release("c1", invalid, "a1")),
                () -> assertThrows(IllegalArgumentException.class, () -> service.release("c1", "s1", invalid)),
                () -> assertThrows(IllegalArgumentException.class, () -> service.isHeldBy(invalid, "s1", "a1")),
                () -> assertThrows(IllegalArgumentException.class, () -> service.isHeldBy("c1", invalid, "a1")),
                () -> assertThrows(IllegalArgumentException.class, () -> service.isHeldBy("c1", "s1", invalid)));
        assertTrue(service.isHeldBy("c1", "s1", "a1"));
    }

    @Test
    void competingCustomersHaveExactlyOneWinner() throws Exception {
        CountDownLatch start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> {
                start.await();
                return service.hold("c1", "s1", "a1");
            });
            var second = executor.submit(() -> {
                start.await();
                return service.hold("c2", "s1", "a1");
            });
            start.countDown();
            boolean firstWon = first.get(5, TimeUnit.SECONDS);
            boolean secondWon = second.get(5, TimeUnit.SECONDS);
            assertNotEquals(firstWon, secondWon);
            assertEquals(firstWon, service.isHeldBy("c1", "s1", "a1"));
            assertEquals(secondWon, service.isHeldBy("c2", "s1", "a1"));
        }
    }

    @Test
    void permanentBookingRejectsEveryNewHoldIncludingOriginalOwnerAfterRelease() {
        var state = new InMemorySeatState();
        var holds = new SimpleSeatHoldService(state);
        var reservations = new InMemorySeatReservationRepository(state);
        assertTrue(holds.hold("c1", "s1", "a1"));
        reservations.markBooked("s1", "a1", "b1");
        assertTrue(holds.isHeldBy("c1", "s1", "a1"));
        assertFalse(holds.hold("c1", "s1", "a1"));
        assertFalse(holds.hold("c2", "s1", "a1"));
        holds.release("c1", "s1", "a1");
        assertFalse(holds.hold("c1", "s1", "a1"));
        assertFalse(holds.hold("c2", "s1", "a1"));
        assertFalse(holds.isHeld("s1", "a1"));
        assertTrue(reservations.isBooked("s1", "a1"));
        assertTrue(holds.hold("c2", "s2", "a1"));
    }
}

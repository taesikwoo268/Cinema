package cinema.infrastructure.persistence.inmemory;

import org.junit.jupiter.api.Test;

import cinema.application.common.exceptions.ConflictException;
import cinema.domain.cinema.Seat;
import cinema.domain.cinema.SeatType;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class InMemorySeatRepositoryTest {

    @Test
    void filtersAndSortsByRowNumberAndId() {
        var repository = new InMemorySeatRepository();
        repository.save(new Seat("s2", "h1", "B", 1, SeatType.NORMAL));
        repository.save(new Seat("s1", "h1", "A", 2, SeatType.VIP));
        repository.save(new Seat("s3", "h2", "A", 1, SeatType.NORMAL));
        assertEquals(java.util.List.of("s1", "s2"), repository.findByHallId("h1").stream().map(Seat::getId).toList());
    }

    @Test
    void duplicateIdRejected() {
        var repository = new InMemorySeatRepository();
        repository.save(new Seat("s1", "hall1", "A", 1, SeatType.NORMAL));

        assertThrows(ConflictException.class,
                () -> repository.save(new Seat("s1", "hall1", "B", 2, SeatType.NORMAL)));
    }

    // Can them logic lower case Row
    @Test
    void rejectsDuplicatePositionOnlyWithinHall() {
        var repository = new InMemorySeatRepository();
        repository.save(new Seat("s1", "h1", "a", 1, SeatType.NORMAL));
        assertThrows(ConflictException.class, () -> repository.save(new Seat("s2", "h1", "A", 1, SeatType.VIP)));
        assertDoesNotThrow(() -> repository.save(new Seat("s3", "h2", "A", 1, SeatType.NORMAL)));
    }

    @Test
    void samePositionAcrossHallsAllowed() {
        var repository = new InMemorySeatRepository();
        repository.save(new Seat("s1", "hall1", "A", 1, SeatType.NORMAL));

        assertDoesNotThrow(() ->
                repository.save(new Seat("s2", "hall2", "A", 1, SeatType.NORMAL)));
        assertEquals(1, repository.findByHallId("hall1").size());
        assertEquals(1, repository.findByHallId("hall2").size());
    }

    @Test
    void concurrentDuplicatePositionAllowsOneInsert() throws Exception {
        var repository = new InMemorySeatRepository();
        int threads = 2;

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch doneGate = new CountDownLatch(threads);
        AtomicInteger success = new AtomicInteger();
        AtomicInteger conflict = new AtomicInteger();

        for (int i = 0; i < threads; i++) {
            final String id = "s" + i;
            executor.submit(() -> {
                try {
                    startGate.await();
                    repository.save(new Seat(id, "hall1", "A", 1, SeatType.NORMAL));
                    success.incrementAndGet();
                } catch (ConflictException e) {
                    conflict.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneGate.countDown();
                }
            });
        }

        startGate.countDown();
        assertTrue(doneGate.await(5, TimeUnit.SECONDS),
                "Hai tác vụ phải hoàn tất trong 5 giây");
        executor.shutdownNow();

        assertEquals(1, success.get(), "Chỉ đúng một insert thành công");
        assertEquals(1, conflict.get(), "Chỉ đúng một ConflictException");
        assertEquals(1, repository.findByHallId("hall1").size(),
                "Chỉ một bản ghi được lưu");
    }
}

package cinema.infrastructure.persistence.inmemory;

import org.junit.jupiter.api.Test;

import cinema.application.common.exceptions.ConflictException;
import cinema.domain.cinema.Hall;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHallRepositoryTest {

    @Test
    void filtersAndSortsByNameThenId() {
        var repository = new InMemoryHallRepository();
        repository.save(new Hall("h2", "c1", "Z"));
        repository.save(new Hall("h1", "c1", "A"));
        repository.save(new Hall("h3", "c2", "A"));
        assertEquals(java.util.List.of("h1", "h2"), repository.findByCinemaId("c1").stream().map(Hall::getId).toList());
    }

    @Test
    void duplicateIdRejected() {
        var repository = new InMemoryHallRepository();
        repository.save(new Hall("h1", "c1", "A"));

        assertThrows(ConflictException.class,
                () -> repository.save(new Hall("h1", "c1", "B")));
    }

    @Test
    void sameNameAcrossCinemasAllowed() {
        var repository = new InMemoryHallRepository();
        repository.save(new Hall("h1", "c1", "A"));

        assertDoesNotThrow(() -> repository.save(new Hall("h2", "c2", "A")));
        assertEquals(1, repository.findByCinemaId("c1").size());
        assertEquals(1, repository.findByCinemaId("c2").size());
    }

    // Chua co trim name cua Hall
    @Test
    void rejectsDuplicateNameOnlyWithinCinema() {
        var repository = new InMemoryHallRepository();
        repository.save(new Hall("h1", "c1", "A"));
        assertThrows(ConflictException.class, () -> repository.save(new Hall("h2", "c1", " A ")));
        assertDoesNotThrow(() -> repository.save(new Hall("h3", "c2", "A")));
    }

    @Test
    void concurrentDuplicateNameAllowsOneInsert() throws Exception {
        var repository = new InMemoryHallRepository();
        int threads = 2;

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch doneGate = new CountDownLatch(threads);
        AtomicInteger success = new AtomicInteger();
        AtomicInteger conflict = new AtomicInteger();

        for (int i = 0; i < threads; i++) {
            final String id = "h" + i;
            executor.submit(() -> {
                try {
                    startGate.await();
                    repository.save(new Hall(id, "c1", "A"));
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
        assertEquals(1, repository.findByCinemaId("c1").size(),
                "Chỉ một bản ghi được lưu");
    }
}

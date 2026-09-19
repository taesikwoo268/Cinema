package cinema.infrastructure.persistence.inmemory;

import org.junit.jupiter.api.Test;

import cinema.application.common.exceptions.ConflictException;
import cinema.domain.cinema.Cinema;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryCinemaRepositoryTest {

    @Test
    void saveAndFind() {
        var repository = new InMemoryCinemaRepository();
        var cinema = new Cinema("c1", "Two", "B");

        repository.save(cinema);

        Optional<Cinema> found = repository.findById("c1");
        assertTrue(found.isPresent());
        assertEquals("c1", found.get().getId());
        assertEquals("Two", found.get().getName());
    }

    @Test
    void savesFindsAndSortsSnapshot() {
        var repository = new InMemoryCinemaRepository();
        repository.save(new Cinema("c2", "Two", "B"));
        repository.save(new Cinema("c1", "One", "A"));
        var result = repository.findAll();
        assertEquals(java.util.List.of("c1", "c2"), result.stream().map(Cinema::getId).toList());
        assertThrows(UnsupportedOperationException.class, () -> result.add(result.get(0)));
    }

    @Test
    void missingIdReturnsEmpty() {
        var repository = new InMemoryCinemaRepository();

        assertTrue(repository.findById("nope").isEmpty());
    }

    @Test
    void rejectsDuplicateId() {
        var repository = new InMemoryCinemaRepository();
        repository.save(new Cinema("c1", "One", "A"));
        assertThrows(ConflictException.class, () -> repository.save(new Cinema("c1", "Again", "B")));
    }
}

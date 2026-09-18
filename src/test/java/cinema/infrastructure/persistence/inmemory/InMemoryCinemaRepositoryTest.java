package cinema.infrastructure.persistence.inmemory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import cinema.application.common.exceptions.ConflictException;
import cinema.domain.cinema.Cinema;

class InMemoryCinemaRepositoryTest {

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
    void rejectsDuplicateId() {
        var repository = new InMemoryCinemaRepository();
        repository.save(new Cinema("c1", "One", "A"));
        assertThrows(ConflictException.class, () -> repository.save(new Cinema("c1", "Again", "B")));
    }
}

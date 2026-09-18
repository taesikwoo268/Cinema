package cinema.infrastructure.persistence.inmemory;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import cinema.application.common.exceptions.ConflictException;
import cinema.domain.cinema.Hall;

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
    void rejectsDuplicateNameOnlyWithinCinema() {
        var repository = new InMemoryHallRepository();
        repository.save(new Hall("h1", "c1", "A"));
        assertThrows(ConflictException.class, () -> repository.save(new Hall("h2", "c1", " A ")));
        assertDoesNotThrow(() -> repository.save(new Hall("h3", "c2", "A")));
    }
}

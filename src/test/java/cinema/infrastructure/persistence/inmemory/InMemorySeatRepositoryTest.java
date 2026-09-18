package cinema.infrastructure.persistence.inmemory;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import cinema.application.common.exceptions.ConflictException;
import cinema.domain.cinema.Seat;
import cinema.domain.cinema.SeatType;

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
    void rejectsDuplicatePositionOnlyWithinHall() {
        var repository = new InMemorySeatRepository();
        repository.save(new Seat("s1", "h1", "a", 1, SeatType.NORMAL));
        assertThrows(ConflictException.class, () -> repository.save(new Seat("s2", "h1", "A", 1, SeatType.VIP)));
        assertDoesNotThrow(() -> repository.save(new Seat("s3", "h2", "A", 1, SeatType.NORMAL)));
    }
}

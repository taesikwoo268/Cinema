package cinema.infrastructure.persistence.inmemory;

import cinema.domain.cinema.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class VenueSnapshotTest {
    @Test
    void listsCannotModifyStorageAndDoNotChangeAfterLaterInserts() {
        var cinemas = new InMemoryCinemaRepository();
        var halls = new InMemoryHallRepository();
        var seats = new InMemorySeatRepository();
        cinemas.save(new Cinema("c1", "Cinema", "Address"));
        halls.save(new Hall("h1", "c1", "Hall"));
        seats.save(new Seat("s1", "h1", "A", 1, SeatType.NORMAL));
        var cinemaSnapshot = cinemas.findAll();
        var hallSnapshot = halls.findByCinemaId("c1");
        var seatSnapshot = seats.findByHallId("h1");
        assertThrows(UnsupportedOperationException.class, cinemaSnapshot::clear);
        assertThrows(UnsupportedOperationException.class, hallSnapshot::clear);
        assertThrows(UnsupportedOperationException.class, seatSnapshot::clear);
        cinemas.save(new Cinema("c2", "Cinema", "Address"));
        halls.save(new Hall("h2", "c1", "Other"));
        seats.save(new Seat("s2", "h1", "A", 2, SeatType.VIP));
        assertEquals(1, cinemaSnapshot.size());
        assertEquals(1, hallSnapshot.size());
        assertEquals(1, seatSnapshot.size());
        assertEquals(2, cinemas.findAll().size());
        assertEquals(2, halls.findByCinemaId("c1").size());
        assertEquals(2, seats.findByHallId("h1").size());
    }
}

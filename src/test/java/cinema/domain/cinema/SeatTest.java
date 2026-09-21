package cinema.domain.cinema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import cinema.domain.common.DomainException;

class SeatTest {

    @Test
    void rejectsInvalidReferences() {
        assertThrows(DomainException.class, () -> new Seat("s1", " ", "A", 1, SeatType.NORMAL));
        assertThrows(DomainException.class, () -> new Seat("s1", "h1", " ", 1, SeatType.NORMAL));
    }

    @Test
    void rejectsNonPositiveNumber() {
        assertThrows(DomainException.class, () -> new Seat("s1", "h1", "A", 0, SeatType.NORMAL));
        assertThrows(DomainException.class, () -> new Seat("s1", "h1", "A", -1, SeatType.NORMAL));
    }

    @Test
    void rejectsNullType() {
        assertThrows(DomainException.class, () -> new Seat("s1", "h1", "A", 1, null));
    }

    @Test
    void normalizesRowAndPreservesFields() {
        var seat = new Seat("s1", "h1", " a ", 2, SeatType.VIP);
        assertEquals("A", seat.getRow());
        assertEquals(2, seat.getNumber());
        assertEquals("h1", seat.getHallId());
    }

    @Test
    void acceptsEachSeatType() {
        for (var type : SeatType.values()) {
            assertEquals(type, new Seat(type.name(), "h1", "A", 1, type).getType());
        }
    }
}

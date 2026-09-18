package cinema.domain.cinema;

import cinema.domain.common.DomainException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SeatTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void rejectsBlankHallId(String hallId) {
        assertThrows(DomainException.class, () -> new Seat("s1", hallId, "A", 1, SeatType.NORMAL));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void rejectsBlankRow(String row) {
        assertThrows(DomainException.class, () -> new Seat("s1", "h1", row, 1, SeatType.NORMAL));
    }

    @ParameterizedTest
    @ValueSource( ints = {-2 ,-1, 0})
    void rejectsNonPositiveNumber(int number) {
        assertThrows(DomainException.class, () -> new Seat("s1", "h1", "A", number, SeatType.NORMAL));
    }

    @Test
    void rejectsNullType() {
        assertThrows(DomainException.class, () -> new Seat("s1", "h1", "A", 1, null));
    }

    @Test
    void acceptsEachSeatType() {
        for (SeatType type : SeatType.values()) {
            assertEquals(type, new Seat("s1", "h1", "A", 1, type).getType());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "A"})
    void normalizesRow(String row) {
        Seat seat = new Seat("s1", "h1", row, 1, SeatType.NORMAL);
        assertEquals(row.toUpperCase(Locale.ROOT), seat.getRow());
    }

    @Test
    void preservesNumberAndHallId() {
        Seat seat = new Seat("s1", "h1", "A", 1, SeatType.NORMAL);
        assertEquals("h1", seat.getHallId());
        assertEquals(1, seat.getNumber());
    }
}

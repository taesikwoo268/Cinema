package cinema.domain.cinema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import cinema.domain.common.DomainException;

class CinemaTest {

    @Test
    void rejectsBlankName() {
        assertThrows(DomainException.class, () -> new Cinema("c1", " ", "Address"));
    }

    @Test
    void rejectsBlankAddress() {
        assertThrows(DomainException.class, () -> new Cinema("c1", "Cinema", " "));
    }

    @Test
    void preservesValidFieldsTrimmed() {
        var cinema = new Cinema("c1", " Cinema ", " Address ");
        assertEquals("Cinema", cinema.getName());
        assertEquals("Address", cinema.getAddress());
    }
}

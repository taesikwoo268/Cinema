package cinema.domain.cinema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import cinema.domain.common.DomainException;

class HallTest {

    @Test
    void rejectsBlankCinemaId() {
        assertThrows(DomainException.class, () -> new Hall("h1", " ", "Hall"));
    }

    @Test
    void rejectsBlankName() {
        assertThrows(DomainException.class, () -> new Hall("h1", "c1", " "));
    }

    @Test
    void preservesParentAndNormalizesName() {
        var hall = new Hall("h1", "c1", " Hall A ");
        assertEquals("c1", hall.getCinemaId());
        assertEquals("Hall A", hall.getName());
    }
}

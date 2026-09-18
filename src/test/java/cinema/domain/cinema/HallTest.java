package cinema.domain.cinema;

import cinema.domain.common.DomainException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class HallTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void rejectsBlankCinemaId(String cinema_id) {
        assertThrows(DomainException.class, () -> new Hall("h1", cinema_id, "Hall-1"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void rejectsBlankName(String name) {
        assertThrows(DomainException.class, () -> new Hall("h1", "c1", name));
    }

    @ParameterizedTest
    @CsvSource({"'c1', ' Hall-1 '"})
    void preservesCinemaIdAndNormalizesName(String cinemaId, String name) {
        Hall hall = new Hall("h1", cinemaId, name);

        assertEquals(cinemaId, hall.getCinemaId());
        assertEquals(name.trim(), hall.getName());
    }
}

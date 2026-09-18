package cinema.domain.cinema;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.assertThrows;
import cinema.domain.common.DomainException;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CinemaTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void rejectsBlankName(String name) {
        assertThrows(DomainException.class, () -> new Cinema("c1", name, "Address-1"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void rejectsBlankAddress(String address) {
        assertThrows(DomainException.class, () -> new Cinema("c1", "Cinema-1", address));
    }

    @ParameterizedTest
    @CsvSource({"' Cinema-1 ', ' Ha Noi '",
            "' Cinema-2', 'Da Nang '",
            "'Cinema-3 ', ' Ho Chi Minh'"
    })
    void preservesValidFields(String name, String address) {
        Cinema cinema_ = new Cinema("c1", name, address);
        assertEquals(name.trim(), cinema_.getName());
        assertEquals(address.trim(), cinema_.getAddress());
    }
}

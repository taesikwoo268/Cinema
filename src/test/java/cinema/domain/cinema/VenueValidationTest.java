package cinema.domain.cinema;

import cinema.domain.common.DomainException;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class VenueValidationTest {
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t\n"})
    void rejectsAllMissingTextFields(String value) {
        assertThrows(DomainException.class, () -> new Cinema("c1", value, "Address"));
        assertThrows(DomainException.class, () -> new Cinema("c1", "Cinema", value));
        assertThrows(DomainException.class, () -> new Hall("h1", value, "Hall"));
        assertThrows(DomainException.class, () -> new Hall("h1", "c1", value));
        assertThrows(DomainException.class, () -> new Seat("s1", value, "A", 1, SeatType.NORMAL));
        assertThrows(DomainException.class, () -> new Seat("s1", "h1", value, 1, SeatType.NORMAL));
    }

    @Test
    void preservesReferenceIdsExactly() {
        assertEquals(" c1 ", new Hall("h1", " c1 ", "Hall").getCinemaId());
        assertEquals(" h1 ", new Seat("s1", " h1 ", "A", 1, SeatType.NORMAL).getHallId());
    }

    @Test
    void rowNormalizationIsLocaleIndependentAndAllowsLongLabels() {
        var previous = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertEquals("I-12", new Seat("s1", "h1", " i-12 ", 1, SeatType.NORMAL).getRow());
        } finally {
            Locale.setDefault(previous);
        }
    }
}

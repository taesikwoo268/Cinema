package cinema.domain.booking;

import cinema.domain.common.DomainException;
import cinema.domain.common.Money;
import java.math.BigDecimal;
import java.util.Currency;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class BookingItemTest {
    private final Money price = new Money(BigDecimal.ZERO, Currency.getInstance("USD"));

    @Test
    void retainsSeatAndPriceIncludingZero() {
        BookingItem item = new BookingItem("seat-1", price);
        assertEquals("seat-1", item.seatId());
        assertSame(price, item.price());
    }
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t\n"})
    void rejectsInvalidSeatId(String seatId) {
        assertThrows(DomainException.class, () -> new BookingItem(seatId, price));
    }
    @Test
    void rejectsNullPrice() {
        assertThrows(DomainException.class, () -> new BookingItem("seat-1", null));
    }
}

package cinema.domain.cart;

import cinema.domain.common.DomainException;
import cinema.domain.common.Money;
import java.math.BigDecimal;
import java.util.Currency;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class CartItemTest {
    private static final Money PRICE = new Money(new BigDecimal("12.50"), Currency.getInstance("USD"));

    @Test
    void retainsValues() {
        CartItem item = new CartItem("screening-1", "seat-1", PRICE);
        assertEquals("screening-1", item.screeningId());
        assertEquals("seat-1", item.seatId());
        assertEquals(PRICE, item.unitPrice());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t\n"})
    void rejectsInvalidScreeningId(String id) {
        assertThrows(DomainException.class, () -> new CartItem(id, "seat-1", PRICE));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t\n"})
    void rejectsInvalidSeatId(String id) {
        assertThrows(DomainException.class, () -> new CartItem("screening-1", id, PRICE));
    }

    @Test
    void rejectsNullPrice() {
        assertThrows(DomainException.class, () -> new CartItem("screening-1", "seat-1", null));
    }

    @Test
    void acceptsZeroPrice() {
        Money zero = new Money(BigDecimal.ZERO, Currency.getInstance("USD"));
        assertEquals(zero, new CartItem("screening-1", "seat-1", zero).unitPrice());
    }
}

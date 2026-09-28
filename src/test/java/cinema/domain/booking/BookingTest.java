package cinema.domain.booking;

import cinema.domain.common.DomainException;
import cinema.domain.common.Money;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import static org.junit.jupiter.api.Assertions.*;

class BookingTest {
    private static final Instant CREATED = Instant.parse("2026-09-28T00:00:00Z");
    private Money money(String amount, String currency) {
        return new Money(new BigDecimal(amount), Currency.getInstance(currency));
    }
    private BookingItem item(String seat, String amount) {
        return new BookingItem(seat, money(amount, "USD"));
    }
    private Booking booking(List<BookingItem> items, BookingStatus status) {
        return new Booking("b1", "c1", "s1", items, money("99.00", "USD"), status, CREATED);
    }

    @Test
    void rejectsEmptyItems() {
        assertThrows(DomainException.class, () -> booking(List.of(), BookingStatus.PENDING));
    }
    @Test
    void rejectsNullItems() {
        assertThrows(DomainException.class, () -> booking(null, BookingStatus.PENDING));
    }
    @Test
    void rejectsNullElement() {
        assertThrows(DomainException.class, () -> booking(Arrays.asList(item("a1", "1"), null), BookingStatus.PENDING));
    }
    @Test
    void retainsSnapshotFieldsAndCalculatesSingleItemWithoutMutatingStoredTotal() {
        Booking booking = booking(List.of(item("a1", "12.50")), BookingStatus.PENDING);
        assertEquals("b1", booking.getId());
        assertEquals("c1", booking.getCustomerId());
        assertEquals("s1", booking.getScreeningId());
        assertEquals(CREATED, booking.getCreatedAt());
        assertEquals(BookingStatus.PENDING, booking.getStatus());
        assertEquals(money("12.50", "USD"), booking.calculateTotal());
        assertEquals(money("99.00", "USD"), booking.getTotalAmount());
    }
    @Test
    void sumsSameCurrencyWithoutRounding() {
        Booking booking = booking(List.of(item("a1", "12.50"), item("a2", "0.125"), item("a3", "0")), BookingStatus.PENDING);
        assertEquals(money("12.625", "USD"), booking.calculateTotal());
    }
    @Test
    void mixedCurrenciesCannotBeSummed() {
        Booking booking = booking(List.of(item("a1", "10"), new BookingItem("a2", money("20", "EUR"))), BookingStatus.PENDING);
        assertThrows(DomainException.class, booking::calculateTotal);
    }
    @Test
    void confirmsPending() {
        Booking booking = booking(List.of(item("a1", "1")), BookingStatus.PENDING);
        booking.confirm();
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
    }
    @Test
    void cancelsPendingAndCannotThenConfirm() {
        Booking booking = booking(List.of(item("a1", "1")), BookingStatus.PENDING);
        booking.cancel();
        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        assertThrows(DomainException.class, booking::confirm);
        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
    }
    @ParameterizedTest
    @EnumSource(value = BookingStatus.class, names = {"CONFIRMED", "CANCELLED"})
    void terminalStatesRejectBothTransitions(BookingStatus status) {
        Booking booking = booking(List.of(item("a1", "1")), status);
        assertThrows(DomainException.class, booking::confirm);
        assertThrows(DomainException.class, booking::cancel);
        assertEquals(status, booking.getStatus());
    }
    @Test
    void itemCollectionIsDefensivelyCopiedAndImmutable() {
        BookingItem first = item("a1", "1");
        var input = new ArrayList<>(List.of(first));
        Booking booking = booking(input, BookingStatus.PENDING);
        input.clear();
        assertEquals(List.of(first), booking.getItems());
        assertThrows(UnsupportedOperationException.class, () -> booking.getItems().clear());
        assertThrows(UnsupportedOperationException.class, () -> booking.getItems().add(item("a2", "2")));
        assertThrows(UnsupportedOperationException.class, () -> booking.getItems().set(0, item("a2", "2")));
        assertEquals(List.of(first), booking.getItems());
    }
}

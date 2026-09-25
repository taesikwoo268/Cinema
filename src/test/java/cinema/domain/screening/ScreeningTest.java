package cinema.domain.screening;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import cinema.domain.common.DomainException;
import cinema.domain.common.Money;

class ScreeningTest {

    private static final Instant START = Instant.parse("2026-10-01T10:00:00Z");
    private static final Instant END = Instant.parse("2026-10-01T12:00:00Z");
    private static final Money PRICE = price("100000.00");

    private static Money price(String amount) {
        return new Money(new BigDecimal(amount), Currency.getInstance("VND"));
    }

    @Test
    void validScreeningKeepsValuesAndStartsScheduled() {
        var screening = new Screening("s1", " movie-1 ", " hall-1 ", START, END, PRICE);
        assertEquals("s1", screening.getId());
        assertEquals(" movie-1 ", screening.getMovieId());
        assertEquals(" hall-1 ", screening.getHallId());
        assertEquals(START, screening.getStartTime());
        assertEquals(END, screening.getEndTime());
        assertEquals(PRICE, screening.getBasePrice());
        assertEquals(ScreeningStatus.SCHEDULED, screening.getStatus());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" \t\n"})
    void rejectsInvalidId(String id) {
        assertThrows(DomainException.class, () -> new Screening(id, "m1", "h1", START, END, PRICE));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" \t\n"})
    void rejectsInvalidMovieId(String id) {
        assertThrows(DomainException.class, () -> new Screening("s1", id, "h1", START, END, PRICE));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" \t\n"})
    void rejectsInvalidHallId(String id) {
        assertThrows(DomainException.class, () -> new Screening("s1", "m1", id, START, END, PRICE));
    }

    @Test
    void rejectsNullStartTime() {
        assertThrows(DomainException.class, () -> new Screening("s1", "m1", "h1", null, END, PRICE));
    }

    @Test
    void rejectsNullEndTime() {
        assertThrows(DomainException.class, () -> new Screening("s1", "m1", "h1", START, null, PRICE));
    }

    @Test
    void rejectsEqualTimes() {
        assertThrows(DomainException.class, () -> new Screening("s1", "m1", "h1", START, START, PRICE));
    }

    @Test
    void rejectsReversedTimes() {
        assertThrows(DomainException.class, () -> new Screening("s1", "m1", "h1", END, START, PRICE));
    }

    @Test
    void negativeBasePriceIsRejectedByMoneyBeforeScreeningConstruction() {
        assertThrows(DomainException.class,
                () -> new Screening("s1", "m1", "h1", START, END, price("-1")));
    }

    @Test
    void acceptsZeroBasePrice() {
        var zero = price("0");
        var screening = new Screening("s1", "m1", "h1", START, END, zero);
        assertEquals(zero, screening.getBasePrice());
        assertEquals(ScreeningStatus.SCHEDULED, screening.getStatus());
    }

    @Test
    void rejectsNullBasePrice() {
        assertThrows(DomainException.class, () -> new Screening("s1", "m1", "h1", START, END, null));
    }

    @Test
    void explicitStatusConstructorPreservesExistingApi() {
        for (ScreeningStatus status : ScreeningStatus.values()) {
            var screening = new Screening("s1", "m1", "h1", START, END, PRICE, status);
            assertEquals(status, screening.getStatus());
            assertEquals(PRICE, screening.getBasePrice());
        }
    }

    @Test
    void explicitStatusConstructorAlsoValidatesInterval() {
        assertThrows(DomainException.class,
                () -> new Screening("s1", "m1", "h1", END, START, PRICE, ScreeningStatus.SCHEDULED));
    }

    @Test
    void scheduledScreeningCanBeCancelled() {
        var screening = new Screening("s1", "m1", "h1", START, END, PRICE);

        screening.cancel();

        assertEquals(ScreeningStatus.CANCELLED, screening.getStatus());
    }

    @Test
    void cancelledScreeningCannotBeCancelledAgain() {
        var screening = new Screening("s1", "m1", "h1", START, END, PRICE, ScreeningStatus.CANCELLED);

        var exception = assertThrows(DomainException.class, screening::cancel);
        assertEquals(DomainException.class, exception.getClass());
    }

    @Test
    void completedScreeningCannotBeCancelled() {
        var screening = new Screening("s1", "m1", "h1", START, END, PRICE, ScreeningStatus.COMPLETED);

        var exception = assertThrows(DomainException.class, screening::cancel);
        assertEquals(DomainException.class, exception.getClass());
    }
}

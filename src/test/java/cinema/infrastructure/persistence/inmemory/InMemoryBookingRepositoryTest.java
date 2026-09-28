package cinema.infrastructure.persistence.inmemory;

import cinema.domain.booking.Booking;
import cinema.domain.booking.BookingItem;
import cinema.domain.booking.BookingStatus;
import cinema.domain.common.Money;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryBookingRepositoryTest {
    private final InMemoryBookingRepository repository = new InMemoryBookingRepository();

    private Booking booking(String id, String customerId) {
        Money price = new Money(new BigDecimal("12.50"), Currency.getInstance("USD"));
        return new Booking(id, customerId, "screening-1",
                List.of(new BookingItem("seat-1", price)), price,
                BookingStatus.PENDING, Instant.parse("2026-09-28T00:00:00Z"));
    }

    @Test
    void findByIdInitiallyEmpty() {
        assertTrue(repository.findById("missing").isEmpty());
    }

    @Test
    void saveThenFindByIdReturnsBooking() {
        Booking booking = booking("b1", "c1");
        repository.save(booking);
        assertSame(booking, repository.findById("b1").orElseThrow());
    }

    @Test
    void storesMultipleBookingsIndependently() {
        Booking first = booking("b1", "c1");
        Booking second = booking("b2", "c1");
        Booking third = booking("b3", "c2");
        repository.save(first);
        repository.save(second);
        repository.save(third);
        assertSame(first, repository.findById("b1").orElseThrow());
        assertSame(second, repository.findById("b2").orElseThrow());
        assertSame(third, repository.findById("b3").orElseThrow());
        assertTrue(repository.findById("missing").isEmpty());
    }

    @Test
    void filtersByCustomerWithDeterministicAdapterOrder() {
        Booking first = booking("b3", "c1");
        Booking other = booking("b2", "c2");
        Booking last = booking("b1", "c1");
        repository.save(first);
        repository.save(other);
        repository.save(last);
        assertEquals(List.of(first, last), repository.findByCustomerId("c1"));
        assertEquals(List.of(other), repository.findByCustomerId("c2"));
    }

    @Test
    void customerWithNoBookingsReturnsEmpty() {
        assertTrue(repository.findByCustomerId("c1").isEmpty());
        repository.save(booking("b1", "c1"));
        assertTrue(repository.findByCustomerId("unknown").isEmpty());
    }

    @Test
    void sameIdReplacesStoredAggregateIncludingCustomerAssociation() {
        Booking original = booking("b1", "c1");
        Booking replacement = booking("b1", "c2");
        replacement.confirm();
        repository.save(original);
        repository.save(replacement);
        assertSame(replacement, repository.findById("b1").orElseThrow());
        assertEquals(BookingStatus.CONFIRMED, repository.findById("b1").orElseThrow().getStatus());
        assertTrue(repository.findByCustomerId("c1").isEmpty());
        assertEquals(List.of(replacement), repository.findByCustomerId("c2"));
    }

    @Test
    void savesUpdatedExistingAggregate() {
        Booking booking = booking("b1", "c1");
        repository.save(booking);
        booking.cancel();
        repository.save(booking);
        assertEquals(BookingStatus.CANCELLED, repository.findById("b1").orElseThrow().getStatus());
        assertEquals(List.of(booking), repository.findByCustomerId("c1"));
    }

    @Test
    void rejectsNullWithoutChangingStoredState() {
        Booking booking = booking("b1", "c1");
        repository.save(booking);
        assertThrows(NullPointerException.class, () -> repository.save(null));
        assertEquals(List.of(booking), repository.findByCustomerId("c1"));
    }

    @Test
    void customerListIsImmutableSnapshotOfCollection() {
        Booking first = booking("b1", "c1");
        Booking second = booking("b2", "c1");
        repository.save(first);
        List<Booking> snapshot = repository.findByCustomerId("c1");
        assertThrows(UnsupportedOperationException.class, snapshot::clear);
        assertThrows(UnsupportedOperationException.class, () -> snapshot.add(second));
        assertThrows(UnsupportedOperationException.class, () -> snapshot.set(0, second));
        assertSame(first, repository.findById("b1").orElseThrow());
        assertTrue(repository.findById("b2").isEmpty());
        repository.save(second);
        repository.save(booking("b1", "c2"));
        assertEquals(List.of(first), snapshot);
        assertEquals(List.of(second), repository.findByCustomerId("c1"));
    }
}

package cinema.infrastructure.persistence.inmemory;

import cinema.application.common.interfaces.BookingRepository;
import cinema.domain.booking.Booking;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Synchronized repository operations; stored aggregates remain mutable references. */
public final class InMemoryBookingRepository implements BookingRepository {
    private final Map<String, Booking> bookings = new LinkedHashMap<>();

    @Override
    public synchronized Optional<Booking> findById(String id) {
        return Optional.ofNullable(bookings.get(id));
    }

    @Override
    public synchronized List<Booking> findByCustomerId(String customerId) {
        return bookings.values().stream()
                .filter(booking -> Objects.equals(booking.getCustomerId(), customerId))
                .toList();
    }

    @Override
    public synchronized void save(Booking booking) {
        Objects.requireNonNull(booking, "Booking must not be null");
        bookings.put(booking.getId(), booking);
    }
}

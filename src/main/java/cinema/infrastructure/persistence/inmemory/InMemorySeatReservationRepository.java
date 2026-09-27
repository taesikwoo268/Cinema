package cinema.infrastructure.persistence.inmemory;

import cinema.application.common.exceptions.ConflictException;
import cinema.application.common.interfaces.SeatReservationRepository;
import java.util.HashMap;
import java.util.Map;

public final class InMemorySeatReservationRepository implements SeatReservationRepository {
    private record SeatKey(String screeningId, String seatId) { }

    private final Map<SeatKey, String> bookings = new HashMap<>();

    @Override
    public synchronized boolean isBooked(String screeningId, String seatId) {
        validateId(screeningId, "screeningId");
        validateId(seatId, "seatId");
        return bookings.containsKey(new SeatKey(screeningId, seatId));
    }

    @Override
    public synchronized void markBooked(String screeningId, String seatId, String bookingId) {
        validateId(screeningId, "screeningId");
        validateId(seatId, "seatId");
        validateId(bookingId, "bookingId");
        String existing = bookings.putIfAbsent(new SeatKey(screeningId, seatId), bookingId);
        if (existing != null && !existing.equals(bookingId)) {
            throw new ConflictException("Seat already booked for screening");
        }
    }

    private static void validateId(String id, String name) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }
}

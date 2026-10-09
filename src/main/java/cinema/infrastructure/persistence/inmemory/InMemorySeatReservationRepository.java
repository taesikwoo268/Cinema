package cinema.infrastructure.persistence.inmemory;

import cinema.application.common.exceptions.ConflictException;
import cinema.application.common.interfaces.SeatReservationRepository;
import java.util.Objects;
import cinema.infrastructure.persistence.inmemory.InMemorySeatState.SeatKey;

public final class InMemorySeatReservationRepository implements SeatReservationRepository {
    private final InMemorySeatState state;

    public InMemorySeatReservationRepository() { this(new InMemorySeatState()); }

    public InMemorySeatReservationRepository(InMemorySeatState state) {
        this.state = Objects.requireNonNull(state);
    }

    @Override
    public boolean isBooked(String screeningId, String seatId) {
        validateId(screeningId, "screeningId");
        validateId(seatId, "seatId");
        synchronized (state) {
            return state.bookings.containsKey(new SeatKey(screeningId, seatId));
        }
    }

    @Override
    public void markBooked(String screeningId, String seatId, String bookingId) {
        validateId(screeningId, "screeningId");
        validateId(seatId, "seatId");
        validateId(bookingId, "bookingId");
        synchronized (state) {
            String existing = state.bookings.putIfAbsent(new SeatKey(screeningId, seatId), bookingId);
            if (existing != null && !existing.equals(bookingId)) {
                throw new ConflictException("Seat already booked for screening");
            }
        }
    }

    private static void validateId(String id, String name) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }
}

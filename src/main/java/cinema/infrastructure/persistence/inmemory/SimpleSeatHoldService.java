package cinema.infrastructure.persistence.inmemory;

import cinema.application.common.interfaces.SeatHoldService;
import java.util.Objects;
import cinema.infrastructure.persistence.inmemory.InMemorySeatState.SeatKey;

/** Simple in-memory seat ownership without expiration. */
public final class SimpleSeatHoldService implements SeatHoldService {
    private final InMemorySeatState state;

    public SimpleSeatHoldService() { this(new InMemorySeatState()); }

    public SimpleSeatHoldService(InMemorySeatState state) {
        this.state = Objects.requireNonNull(state);
    }

    @Override
    public boolean hold(String customerId, String screeningId, String seatId) {
        validate(customerId, screeningId, seatId);
        synchronized (state) {
            var key = new SeatKey(screeningId, seatId);
            if (state.bookings.containsKey(key)) return false;
            String owner = state.owners.putIfAbsent(key, customerId);
            return owner == null || owner.equals(customerId);
        }
    }

    @Override
    public void release(String customerId, String screeningId, String seatId) {
        validate(customerId, screeningId, seatId);
        synchronized (state) {
            state.owners.remove(new SeatKey(screeningId, seatId), customerId);
        }
    }

    @Override
    public boolean isHeldBy(String customerId, String screeningId, String seatId) {
        validate(customerId, screeningId, seatId);
        synchronized (state) {
            return customerId.equals(state.owners.get(new SeatKey(screeningId, seatId)));
        }
    }

    @Override
    public boolean isHeld(String screeningId, String seatId) {
        validate("availability", screeningId, seatId);
        synchronized (state) {
            return state.owners.containsKey(new SeatKey(screeningId, seatId));
        }
    }

    private static void validate(String customerId, String screeningId, String seatId) {
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("customerId must not be blank");
        }
        if (screeningId == null || screeningId.isBlank()) {
            throw new IllegalArgumentException("screeningId must not be blank");
        }
        if (seatId == null || seatId.isBlank()) {
            throw new IllegalArgumentException("seatId must not be blank");
        }
    }
}

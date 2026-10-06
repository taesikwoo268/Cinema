package cinema.infrastructure.persistence.inmemory;

import cinema.application.common.interfaces.SeatHoldService;
import java.util.HashMap;
import java.util.Map;

/** Simple in-memory seat ownership without expiration. */
public final class SimpleSeatHoldService implements SeatHoldService {
    private record SeatKey(String screeningId, String seatId) { }

    private final Map<SeatKey, String> owners = new HashMap<>();

    @Override
    public synchronized boolean hold(String customerId, String screeningId, String seatId) {
        validate(customerId, screeningId, seatId);
        String owner = owners.putIfAbsent(new SeatKey(screeningId, seatId), customerId);
        return owner == null || owner.equals(customerId);
    }

    @Override
    public synchronized void release(String customerId, String screeningId, String seatId) {
        validate(customerId, screeningId, seatId);
        owners.remove(new SeatKey(screeningId, seatId), customerId);
    }

    @Override
    public synchronized boolean isHeldBy(String customerId, String screeningId, String seatId) {
        validate(customerId, screeningId, seatId);
        return customerId.equals(owners.get(new SeatKey(screeningId, seatId)));
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

package cinema.infrastructure.persistence.inmemory;

import java.util.HashMap;
import java.util.Map;

/** Both seat adapters guard all state access with this instance's monitor. */
public final class InMemorySeatState {
    record SeatKey(String screeningId, String seatId) { }
    final Map<SeatKey, String> owners = new HashMap<>();
    final Map<SeatKey, String> bookings = new HashMap<>();
}

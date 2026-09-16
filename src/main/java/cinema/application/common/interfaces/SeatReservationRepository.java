package cinema.application.common.interfaces;

import java.util.List;
import java.util.Optional;
import java.time.Instant;
public interface SeatReservationRepository {
    boolean isBooked(String screeningId, String seatId);
    void markBooked(String screeningId, String seatId, String bookingId);
}

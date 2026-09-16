package cinema.infrastructure.seat_hold;

import cinema.application.common.interfaces.SeatHoldService;
import cinema.application.common.models.*;
import cinema.domain.common.Money;
/** Adapter skeleton; not registered as a working provider. */
public final class DatabaseSeatHoldService implements SeatHoldService {
    @Override public boolean hold(String customerId, String screeningId, String seatId) { throw new UnsupportedOperationException("Architecture Contract v0.1: not implemented" ); }
    @Override public void release(String customerId, String screeningId, String seatId) { throw new UnsupportedOperationException("Architecture Contract v0.1: not implemented" ); }
    @Override public boolean isHeldBy(String customerId, String screeningId, String seatId) { throw new UnsupportedOperationException("Architecture Contract v0.1: not implemented" ); }
}

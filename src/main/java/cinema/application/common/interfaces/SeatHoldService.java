package cinema.application.common.interfaces;

import cinema.application.common.models.*;
import cinema.domain.common.Money;
import java.time.Instant;
import java.util.Optional;
public interface SeatHoldService { boolean hold(String customerId, String screeningId, String seatId); void release(String customerId, String screeningId, String seatId); boolean isHeldBy(String customerId, String screeningId, String seatId); }

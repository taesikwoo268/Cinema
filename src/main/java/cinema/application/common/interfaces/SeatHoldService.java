package cinema.application.common.interfaces;

public interface SeatHoldService {

    boolean hold(String customerId, String screeningId, String seatId);

    void release(String customerId, String screeningId, String seatId);

    boolean isHeldBy(String customerId, String screeningId, String seatId);
}

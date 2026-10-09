package cinema.infrastructure.seat_hold;

import cinema.application.common.interfaces.SeatHoldService;
import cinema.application.common.exceptions.NotFoundException;
import cinema.infrastructure.persistence.entities.SeatHoldJpaEntity;
import cinema.infrastructure.persistence.repositories.*;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@Profile("mysql")
@Transactional
public class DatabaseSeatHoldService implements SeatHoldService {
    private final SpringDataSeatHoldRepository holds;
    private final SpringDataSeatReservationRepository reservations;
    private final SpringDataScreeningRepository screenings;
    public DatabaseSeatHoldService(SpringDataSeatHoldRepository holds, SpringDataSeatReservationRepository reservations,
            SpringDataScreeningRepository screenings) {
        this.holds = holds; this.reservations = reservations; this.screenings = screenings;
    }
    private void lock(String screeningId) {
        screenings.lockById(screeningId).orElseThrow(() -> new NotFoundException("Screening not found"));
    }
    @Override public boolean hold(String customerId, String screeningId, String seatId) {
        lock(screeningId);
        if (reservations.findByScreeningIdAndSeatId(screeningId, seatId).isPresent()) return false;
        var held = holds.findByScreeningIdAndSeatId(screeningId, seatId);
        if (held.isPresent()) return held.get().customerId.equals(customerId);
        var e = new SeatHoldJpaEntity();
        e.id = UUID.randomUUID().toString(); e.customerId = customerId; e.screeningId = screeningId; e.seatId = seatId;
        holds.saveAndFlush(e);
        return true;
    }
    @Override public void release(String customerId, String screeningId, String seatId) {
        lock(screeningId);
        holds.findByScreeningIdAndSeatId(screeningId, seatId).filter(e -> e.customerId.equals(customerId)).ifPresent(holds::delete);
        holds.flush();
    }
    @Override public boolean isHeldBy(String customerId, String screeningId, String seatId) {
        lock(screeningId);
        return holds.findByScreeningIdAndSeatId(screeningId, seatId).map(e -> e.customerId.equals(customerId)).orElse(false);
    }
    @Override public boolean isHeld(String screeningId, String seatId) {
        lock(screeningId);
        return holds.findByScreeningIdAndSeatId(screeningId, seatId).isPresent();
    }
}

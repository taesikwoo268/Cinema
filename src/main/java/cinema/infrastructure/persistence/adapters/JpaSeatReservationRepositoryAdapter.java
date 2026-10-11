package cinema.infrastructure.persistence.adapters;

import cinema.application.common.exceptions.*;
import cinema.application.common.interfaces.SeatReservationRepository;
import cinema.infrastructure.persistence.entities.SeatReservationJpaEntity;
import cinema.infrastructure.persistence.repositories.*;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Repository
@Profile("mysql")
@Transactional
public class JpaSeatReservationRepositoryAdapter implements SeatReservationRepository {
    private final SpringDataSeatReservationRepository data;
    private final SpringDataScreeningRepository screenings;
    public JpaSeatReservationRepositoryAdapter(SpringDataSeatReservationRepository data, SpringDataScreeningRepository screenings) {
        this.data = data;
        this.screenings = screenings;
    }
    @Override public boolean isBooked(String screeningId, String seatId) {
        lock(screeningId);
        return data.findByScreeningIdAndSeatId(screeningId, seatId).isPresent();
    }
    @Override public void markBooked(String screeningId, String seatId, String bookingId) {
        lock(screeningId);
        var existing = data.findByScreeningIdAndSeatId(screeningId, seatId);
        if (existing.isPresent()) {
            if (!existing.get().bookingId.equals(bookingId)) throw new ConflictException("Seat already booked for screening");
            return;
        }
        var e = new SeatReservationJpaEntity();
        e.id = UUID.randomUUID().toString(); e.screeningId = screeningId; e.seatId = seatId; e.bookingId = bookingId;
        data.saveAndFlush(e);
    }
    private void lock(String screeningId) {
        screenings.lockById(screeningId).orElseThrow(() -> new NotFoundException("Screening not found"));
    }
}

package cinema.application.screenings.queries;

import cinema.application.common.interfaces.*;
import cinema.application.common.models.SeatView;
import cinema.application.common.exceptions.*;
import cinema.domain.screening.ScreeningStatus;
import java.util.List;

public final class GetAvailableSeatsQueryHandler implements UseCase<GetAvailableSeatsQuery, List<SeatView>> {
    private final ScreeningRepository screenings;
    private final SeatRepository seats;
    private final SeatReservationRepository reservations;
    private final SeatHoldService holds;
    public GetAvailableSeatsQueryHandler(ScreeningRepository screenings, SeatRepository seats,
            SeatReservationRepository reservations, SeatHoldService holds) {
        this.screenings = screenings;
        this.seats = seats;
        this.reservations = reservations;
        this.holds = holds;
    }
    @Override public List<SeatView> handle(GetAvailableSeatsQuery input) {
        if (input == null || input.screeningId() == null || input.screeningId().isBlank()) {
            throw new ValidationException("Screening ID must not be blank");
        }
        var screening = screenings.findById(input.screeningId()).orElseThrow(() -> new NotFoundException("Screening not found"));
        return seats.findByHallId(screening.getHallId()).stream()
                .map(s -> new SeatView(s.getId(), s.getRow(), s.getNumber(), s.getType().name(),
                        screening.getStatus() == ScreeningStatus.SCHEDULED
                        && !reservations.isBooked(screening.getId(), s.getId())
                        && !holds.isHeld(screening.getId(), s.getId())))
                .toList();
    }
}

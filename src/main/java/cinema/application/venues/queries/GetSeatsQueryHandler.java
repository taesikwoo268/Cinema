package cinema.application.venues.queries;

import cinema.application.common.exceptions.NotFoundException;
import cinema.application.common.exceptions.ValidationException;
import cinema.application.common.interfaces.HallRepository;
import cinema.application.common.interfaces.SeatRepository;
import cinema.application.common.interfaces.UseCase;
import cinema.application.venues.models.PhysicalSeatView;
import cinema.domain.cinema.Seat;
import java.util.List;

public final class GetSeatsQueryHandler implements UseCase<GetSeatsQuery, List<PhysicalSeatView>> {
    private final HallRepository halls;
    private final SeatRepository seats;

    public GetSeatsQueryHandler(HallRepository halls, SeatRepository seats) {
        this.halls = halls;
        this.seats = seats;
    }

    @Override
    public List<PhysicalSeatView> handle(GetSeatsQuery input) {
        if (input == null) {
            throw new ValidationException("GetSeatsQuery must not be null");
        }
        String hallId = input.hallId();
        if (hallId == null || hallId.isBlank()) {
            throw new ValidationException("Hall id must not be blank");
        }
        if (halls.findById(hallId).isEmpty()) {
            throw new NotFoundException("Hall not found: " + hallId);
        }
        return seats.findByHallId(hallId).stream().map(GetSeatsQueryHandler::toView).toList();
    }

    private static PhysicalSeatView toView(Seat seat) {
        return new PhysicalSeatView(seat.getId(), seat.getHallId(), seat.getRow(), seat.getNumber(),
            seat.getType().name());
    }
}

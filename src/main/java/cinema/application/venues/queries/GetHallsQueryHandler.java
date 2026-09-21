package cinema.application.venues.queries;

import cinema.application.common.exceptions.NotFoundException;
import cinema.application.common.exceptions.ValidationException;
import cinema.application.common.interfaces.CinemaRepository;
import cinema.application.common.interfaces.HallRepository;
import cinema.application.common.interfaces.UseCase;
import cinema.application.venues.models.HallView;
import cinema.domain.cinema.Hall;
import java.util.List;

public final class GetHallsQueryHandler implements UseCase<GetHallsQuery, List<HallView>> {
    private final CinemaRepository cinemas;
    private final HallRepository halls;

    public GetHallsQueryHandler(CinemaRepository cinemas, HallRepository halls) {
        this.cinemas = cinemas;
        this.halls = halls;
    }

    @Override
    public List<HallView> handle(GetHallsQuery input) {
        if (input == null) {
            throw new ValidationException("GetHallsQuery must not be null");
        }
        String cinemaId = input.cinemaId();
        if (cinemaId == null || cinemaId.isBlank()) {
            throw new ValidationException("Cinema id must not be blank");
        }
        if (cinemas.findById(cinemaId).isEmpty()) {
            throw new NotFoundException("Cinema not found: " + cinemaId);
        }
        return halls.findByCinemaId(cinemaId).stream().map(GetHallsQueryHandler::toView).toList();
    }

    private static HallView toView(Hall hall) {
        return new HallView(hall.getId(), hall.getCinemaId(), hall.getName());
    }
}

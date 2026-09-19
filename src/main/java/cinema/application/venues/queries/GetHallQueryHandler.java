package cinema.application.venues.queries;

import cinema.application.common.exceptions.NotFoundException;
import cinema.application.common.exceptions.ValidationException;
import cinema.application.common.interfaces.HallRepository;
import cinema.application.common.interfaces.UseCase;
import cinema.application.venues.models.HallView;
import cinema.domain.cinema.Hall;

public final class GetHallQueryHandler implements UseCase<GetHallQuery, HallView> {
    private final HallRepository halls;

    public GetHallQueryHandler(HallRepository halls) {
        this.halls = halls;
    }

    @Override
    public HallView handle(GetHallQuery input) {
        if (input == null) {
            throw new ValidationException("GetHallQuery must not be null");
        }
        String id = input.id();
        if (id == null || id.isBlank()) {
            throw new ValidationException("Hall id must not be blank");
        }
        Hall hall = halls.findById(id)
            .orElseThrow(() -> new NotFoundException("Hall not found: " + id));
        return new HallView(hall.getId(), hall.getCinemaId(), hall.getName());
    }
}

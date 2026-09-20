package cinema.application.venues.queries;

import cinema.application.common.exceptions.NotFoundException;
import cinema.application.common.exceptions.ValidationException;
import cinema.application.common.interfaces.CinemaRepository;
import cinema.application.common.interfaces.UseCase;
import cinema.application.venues.models.CinemaView;
import cinema.domain.cinema.Cinema;

public final class GetCinemaQueryHandler implements UseCase<GetCinemaQuery, CinemaView> {
    private final CinemaRepository cinemas;

    public GetCinemaQueryHandler(CinemaRepository cinemas) {
        this.cinemas = cinemas;
    }

    @Override
    public CinemaView handle(GetCinemaQuery input) {
        if (input == null) {
            throw new ValidationException("GetCinemaQuery must not be null");
        }
        String id = input.id();
        if (id == null || id.isBlank()) {
            throw new ValidationException("Cinema id must not be blank");
        }
        Cinema cinema = cinemas.findById(id)
            .orElseThrow(() -> new NotFoundException("Cinema not found: " + id));
        return new CinemaView(cinema.getId(), cinema.getName(), cinema.getAddress());
    }
}

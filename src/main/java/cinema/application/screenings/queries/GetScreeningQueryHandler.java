package cinema.application.screenings.queries;

import cinema.application.common.exceptions.NotFoundException;
import cinema.application.common.exceptions.ValidationException;
import cinema.application.common.interfaces.ScreeningRepository;
import cinema.application.common.interfaces.UseCase;
import cinema.application.common.models.ScreeningView;
import cinema.domain.screening.Screening;

public final class GetScreeningQueryHandler implements UseCase<GetScreeningQuery, ScreeningView> {

    private final ScreeningRepository screenings;

    public GetScreeningQueryHandler(ScreeningRepository screenings) {
        this.screenings = screenings;
    }

    @Override
    public ScreeningView handle(GetScreeningQuery input) {
        if (input == null) {
            throw new ValidationException("GetScreeningQuery must not be null");
        }
        if (input.screeningId() == null || input.screeningId().isBlank()) {
            throw new ValidationException("Screening ID must not be blank");
        }

        Screening screening = screenings.findById(input.screeningId())
                .orElseThrow(() -> new NotFoundException("Screening not found: " + input.screeningId()));
        return new ScreeningView(
                screening.getId(),
                screening.getMovieId(),
                screening.getHallId(),
                screening.getStartTime(),
                screening.getEndTime(),
                screening.getBasePrice().amount(),
                screening.getBasePrice().currency().getCurrencyCode(),
                screening.getStatus().name());
    }
}

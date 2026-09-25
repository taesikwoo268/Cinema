package cinema.application.screenings.queries;

import java.util.List;

import cinema.application.common.exceptions.ValidationException;
import cinema.application.common.interfaces.ScreeningRepository;
import cinema.application.common.interfaces.UseCase;
import cinema.application.common.models.ScreeningView;
import cinema.domain.screening.Screening;

public final class GetScreeningsQueryHandler implements UseCase<GetScreeningsQuery, List<ScreeningView>> {

    private final ScreeningRepository screenings;

    public GetScreeningsQueryHandler(ScreeningRepository screenings) {
        this.screenings = screenings;
    }

    @Override
    public List<ScreeningView> handle(GetScreeningsQuery input) {
        if (input == null) {
            throw new ValidationException("GetScreeningsQuery must not be null");
        }
        if (input.movieId() == null || input.movieId().isBlank()) {
            throw new ValidationException("Movie ID must not be blank");
        }
        return screenings.findByMovieId(input.movieId()).stream()
                .map(GetScreeningsQueryHandler::toView)
                .toList();
    }

    private static ScreeningView toView(Screening screening) {
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

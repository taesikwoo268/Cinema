package cinema.web.controllers;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cinema.application.common.models.ScreeningView;
import cinema.application.screenings.commands.CancelScreeningCommand;
import cinema.application.screenings.commands.CancelScreeningCommandHandler;
import cinema.application.screenings.commands.CreateScreeningCommand;
import cinema.application.screenings.commands.CreateScreeningCommandHandler;
import cinema.application.screenings.queries.GetAvailableSeatsQueryHandler;
import cinema.application.screenings.queries.GetScreeningQuery;
import cinema.application.screenings.queries.GetScreeningQueryHandler;
import cinema.application.screenings.queries.GetScreeningsQuery;
import cinema.application.screenings.queries.GetScreeningsQueryHandler;
import cinema.web.requests.CreateScreeningRequest;
import cinema.web.responses.CreatedResourceResponse;

/**
 * HTTP methods, request mapping and response mapping are pending
 * implementation.
 */
@RestController
@Profile("venue-dev")
@RequestMapping("/screenings")
public final class ScreeningController {

    private final CreateScreeningCommandHandler createScreeningCommand;
    private final CancelScreeningCommandHandler cancelScreeningCommand;
    private final GetScreeningsQueryHandler getScreeningsQuery;
    private final GetScreeningQueryHandler getScreeningQuery;
    private final GetAvailableSeatsQueryHandler getAvailableSeatsQuery;

    public ScreeningController(CreateScreeningCommandHandler createScreeningCommand, CancelScreeningCommandHandler cancelScreeningCommand, GetScreeningsQueryHandler getScreeningsQuery, GetScreeningQueryHandler getScreeningQuery, GetAvailableSeatsQueryHandler getAvailableSeatsQuery) {
        this.createScreeningCommand = createScreeningCommand;
        this.cancelScreeningCommand = cancelScreeningCommand;
        this.getScreeningsQuery = getScreeningsQuery;
        this.getScreeningQuery = getScreeningQuery;
        this.getAvailableSeatsQuery = getAvailableSeatsQuery;
    }

    @PostMapping
    public ResponseEntity<CreatedResourceResponse> create(@RequestBody CreateScreeningRequest request) {
        String id = createScreeningCommand.handle(new CreateScreeningCommand(
                request.movieId(), request.hallId(), request.startTime(), request.endTime(),
                request.basePrice(), request.currency()));
        return ResponseEntity.status(201).body(new CreatedResourceResponse(id));
    }

    @GetMapping("/{id}")
    public ScreeningView getById(@PathVariable String id) {
        return getScreeningQuery.handle(new GetScreeningQuery(id));
    }

    @GetMapping
    public List<ScreeningView> getByMovie(@RequestParam String movieId) {
        return getScreeningsQuery.handle(new GetScreeningsQuery(movieId));
    }

    @GetMapping("/{id}/available-seats")
    public List<cinema.application.common.models.SeatView> availableSeats(@PathVariable String id) {
        return getAvailableSeatsQuery.handle(new cinema.application.screenings.queries.GetAvailableSeatsQuery(id));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable String id) {
        cancelScreeningCommand.handle(new CancelScreeningCommand(id));
        return ResponseEntity.noContent().build();
    }
}

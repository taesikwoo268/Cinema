package cinema.web.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import cinema.application.screenings.commands.CreateScreeningCommandHandler;
import cinema.application.screenings.commands.CancelScreeningCommandHandler;
import cinema.application.screenings.queries.GetScreeningsQueryHandler;
import cinema.application.screenings.queries.GetScreeningQueryHandler;
import cinema.application.screenings.queries.GetAvailableSeatsQueryHandler;
/** HTTP methods, request mapping and response mapping are pending implementation. */
@RestController
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
}

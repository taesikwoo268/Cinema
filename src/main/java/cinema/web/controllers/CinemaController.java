package cinema.web.controllers;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cinema.application.venues.commands.CreateCinemaCommand;
import cinema.application.venues.commands.CreateCinemaCommandHandler;
import cinema.application.venues.queries.GetCinemaQuery;
import cinema.application.venues.queries.GetCinemaQueryHandler;
import cinema.application.venues.queries.GetCinemasQuery;
import cinema.application.venues.queries.GetCinemasQueryHandler;
import cinema.application.venues.queries.GetHallsQuery;
import cinema.application.venues.queries.GetHallsQueryHandler;
import cinema.web.requests.CreateCinemaRequest;
import cinema.web.responses.CreatedResourceResponse;

@RestController
@Profile("venue-dev")
@RequestMapping("/cinemas")
public final class CinemaController {

    private final CreateCinemaCommandHandler create;
    private final GetCinemasQueryHandler getAll;
    private final GetCinemaQueryHandler getById;
    private final GetHallsQueryHandler getHalls;

    public CinemaController(CreateCinemaCommandHandler create, GetCinemasQueryHandler getAll,
            GetCinemaQueryHandler getById, GetHallsQueryHandler getHalls) {
        this.create = create;
        this.getAll = getAll;
        this.getById = getById;
        this.getHalls = getHalls;
    }

    @PostMapping
    public ResponseEntity<CreatedResourceResponse> create(@RequestBody CreateCinemaRequest request) {
        String id = create.handle(new CreateCinemaCommand(request.name(), request.address()));
        return ResponseEntity.status(201).body(new CreatedResourceResponse(id));
    }

    @GetMapping
    public java.util.List<cinema.application.venues.models.CinemaView> getAll() {
        return getAll.handle(new GetCinemasQuery());
    }

    @GetMapping("/{id}")
    public cinema.application.venues.models.CinemaView getById(@PathVariable String id) {
        return getById.handle(new GetCinemaQuery(id));
    }

    @GetMapping("/{id}/halls")
    public java.util.List<cinema.application.venues.models.HallView> getHalls(@PathVariable String id) {
        return getHalls.handle(new GetHallsQuery(id));
    }
}

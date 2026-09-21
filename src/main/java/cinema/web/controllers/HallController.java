package cinema.web.controllers;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cinema.application.venues.commands.CreateHallCommand;
import cinema.application.venues.commands.CreateHallCommandHandler;
import cinema.application.venues.queries.GetHallQuery;
import cinema.application.venues.queries.GetHallQueryHandler;
import cinema.application.venues.queries.GetSeatsQuery;
import cinema.application.venues.queries.GetSeatsQueryHandler;
import cinema.web.requests.CreateHallRequest;
import cinema.web.responses.CreatedResourceResponse;

@RestController
@Profile("venue-dev")
@RequestMapping("/halls")
public final class HallController {

    private final CreateHallCommandHandler create;
    private final GetHallQueryHandler getById;
    private final GetSeatsQueryHandler getSeats;

    public HallController(CreateHallCommandHandler create, GetHallQueryHandler getById,
            GetSeatsQueryHandler getSeats) {
        this.create = create;
        this.getById = getById;
        this.getSeats = getSeats;
    }

    @PostMapping
    public ResponseEntity<CreatedResourceResponse> create(@RequestBody CreateHallRequest request) {
        String id = create.handle(new CreateHallCommand(request.cinemaId(), request.name()));
        return ResponseEntity.status(201).body(new CreatedResourceResponse(id));
    }

    @GetMapping("/{id}")
    public cinema.application.venues.models.HallView getById(@PathVariable String id) {
        return getById.handle(new GetHallQuery(id));
    }

    @GetMapping("/{id}/seats")
    public java.util.List<cinema.application.venues.models.PhysicalSeatView> getSeats(@PathVariable String id) {
        return getSeats.handle(new GetSeatsQuery(id));
    }
}

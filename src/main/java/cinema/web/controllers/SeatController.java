package cinema.web.controllers;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cinema.application.venues.commands.CreateSeatCommand;
import cinema.application.venues.commands.CreateSeatCommandHandler;
import cinema.web.requests.CreateSeatRequest;
import cinema.web.responses.CreatedResourceResponse;

@RestController
@Profile("venue-dev")
@RequestMapping("/seats")
public final class SeatController {

    private final CreateSeatCommandHandler create;

    public SeatController(CreateSeatCommandHandler create) {
        this.create = create;
    }

    @PostMapping
    public ResponseEntity<CreatedResourceResponse> create(@RequestBody CreateSeatRequest request) {
        String id = create.handle(new CreateSeatCommand(request.hallId(), request.row(), request.number(), request.type()));
        return ResponseEntity.status(201).body(new CreatedResourceResponse(id));
    }
}

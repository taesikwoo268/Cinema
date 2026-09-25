package cinema.web.controllers;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cinema.application.common.models.MovieView;
import cinema.application.movies.commands.CreateMovieCommand;
import cinema.application.movies.commands.CreateMovieCommandHandler;
import cinema.application.movies.commands.DeleteMovieCommand;
import cinema.application.movies.commands.DeleteMovieCommandHandler;
import cinema.application.movies.queries.GetMovieQuery;
import cinema.application.movies.queries.GetMovieQueryHandler;
import cinema.application.movies.queries.GetMoviesQuery;
import cinema.application.movies.queries.GetMoviesQueryHandler;
import cinema.web.requests.CreateMovieRequest;
import cinema.web.responses.CreatedResourceResponse;

@RestController
@Profile("venue-dev")
@RequestMapping("/movies")
public final class MovieController {
    private final CreateMovieCommandHandler createMovieCommand;
    private final DeleteMovieCommandHandler deleteMovieCommand;
    private final GetMoviesQueryHandler getMoviesQuery;
    private final GetMovieQueryHandler getMovieQuery;

    public MovieController(CreateMovieCommandHandler createMovieCommand, DeleteMovieCommandHandler deleteMovieCommand,
            GetMoviesQueryHandler getMoviesQuery, GetMovieQueryHandler getMovieQuery) {
        this.createMovieCommand = createMovieCommand;
        this.deleteMovieCommand = deleteMovieCommand;
        this.getMoviesQuery = getMoviesQuery;
        this.getMovieQuery = getMovieQuery;
    }

    @PostMapping
    public ResponseEntity<CreatedResourceResponse> create(@RequestBody CreateMovieRequest request) {
        String id = createMovieCommand.handle(new CreateMovieCommand(request.title(), request.description(),
                request.durationMinutes(), request.ageRating()));
        return ResponseEntity.status(201).body(new CreatedResourceResponse(id));
    }

    @GetMapping("/{id}")
    public MovieView getById(@PathVariable String id) {
        return getMovieQuery.handle(new GetMovieQuery(id));
    }

    @GetMapping
    public List<MovieView> getAll() {
        return getMoviesQuery.handle(new GetMoviesQuery());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        deleteMovieCommand.handle(new DeleteMovieCommand(id));
        return ResponseEntity.noContent().build();
    }
}

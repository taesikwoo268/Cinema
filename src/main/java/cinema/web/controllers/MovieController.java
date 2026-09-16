package cinema.web.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import cinema.application.movies.commands.CreateMovieCommandHandler;
import cinema.application.movies.commands.DeleteMovieCommandHandler;
import cinema.application.movies.queries.GetMoviesQueryHandler;
import cinema.application.movies.queries.GetMovieQueryHandler;
/** HTTP methods, request mapping and response mapping are pending implementation. */
@RestController
@RequestMapping("/movies")
public final class MovieController {
    private final CreateMovieCommandHandler createMovieCommand;
    private final DeleteMovieCommandHandler deleteMovieCommand;
    private final GetMoviesQueryHandler getMoviesQuery;
    private final GetMovieQueryHandler getMovieQuery;
    public MovieController(CreateMovieCommandHandler createMovieCommand, DeleteMovieCommandHandler deleteMovieCommand, GetMoviesQueryHandler getMoviesQuery, GetMovieQueryHandler getMovieQuery) {
        this.createMovieCommand = createMovieCommand;
        this.deleteMovieCommand = deleteMovieCommand;
        this.getMoviesQuery = getMoviesQuery;
        this.getMovieQuery = getMovieQuery;
    }
}

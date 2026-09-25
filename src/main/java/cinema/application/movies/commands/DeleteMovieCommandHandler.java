package cinema.application.movies.commands;

import cinema.application.common.exceptions.NotFoundException;
import cinema.application.common.exceptions.ValidationException;
import cinema.application.common.interfaces.MovieRepository;
import cinema.application.common.interfaces.UseCase;

public final class DeleteMovieCommandHandler implements UseCase<DeleteMovieCommand, Void> {
    private final MovieRepository movies;

    public DeleteMovieCommandHandler(MovieRepository movies) {
        this.movies = movies;
    }

    @Override
    public Void handle(DeleteMovieCommand input) {
        if (input == null) {
            throw new ValidationException("DeleteMovieCommand must not be null");
        }
        String movieId = input.movieId();
        if (movieId == null || movieId.isBlank()) {
            throw new ValidationException("Movie id must not be blank");
        }
        movies.findById(movieId)
                .orElseThrow(() -> new NotFoundException("Movie not found: " + movieId));
        movies.deleteById(movieId);
        return null;
    }
}

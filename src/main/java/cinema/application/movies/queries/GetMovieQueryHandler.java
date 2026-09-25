package cinema.application.movies.queries;

import cinema.application.common.exceptions.NotFoundException;
import cinema.application.common.exceptions.ValidationException;
import cinema.application.common.interfaces.MovieRepository;
import cinema.application.common.interfaces.UseCase;
import cinema.application.common.models.MovieView;
import cinema.domain.movie.Movie;

public final class GetMovieQueryHandler implements UseCase<GetMovieQuery, MovieView> {
    private final MovieRepository movies;

    public GetMovieQueryHandler(MovieRepository movies) {
        this.movies = movies;
    }

    @Override
    public MovieView handle(GetMovieQuery input) {
        if (input == null) {
            throw new ValidationException("GetMovieQuery must not be null");
        }
        String movieId = input.movieId();
        if (movieId == null || movieId.isBlank()) {
            throw new ValidationException("Movie id must not be blank");
        }
        Movie movie = movies.findById(movieId)
                .orElseThrow(() -> new NotFoundException("Movie not found: " + movieId));
        return new MovieView(movie.getId(), movie.getTitle(), movie.getDescription(),
                movie.getDurationMinutes(), movie.getAgeRating(), movie.getStatus());
    }
}

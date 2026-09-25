package cinema.application.movies.queries;

import cinema.application.common.exceptions.ValidationException;
import cinema.application.common.interfaces.MovieRepository;
import cinema.application.common.interfaces.UseCase;
import cinema.application.common.models.MovieView;
import java.util.List;

public final class GetMoviesQueryHandler implements UseCase<GetMoviesQuery, List<MovieView>> {
    private final MovieRepository movies;

    public GetMoviesQueryHandler(MovieRepository movies) {
        this.movies = movies;
    }

    @Override
    public List<MovieView> handle(GetMoviesQuery input) {
        if (input == null) {
            throw new ValidationException("GetMoviesQuery must not be null");
        }
        return movies.findAll().stream()
                .map(movie -> new MovieView(movie.getId(), movie.getTitle(), movie.getDescription(),
                        movie.getDurationMinutes(), movie.getAgeRating(), movie.getStatus()))
                .toList();
    }
}

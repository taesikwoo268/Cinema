package cinema.application.movies.commands;

import cinema.application.common.exceptions.ValidationException;
import cinema.application.common.interfaces.MovieRepository;
import cinema.application.common.interfaces.UseCase;
import cinema.domain.common.DomainException;
import cinema.domain.movie.Movie;
import cinema.domain.movie.MovieStatus;
import java.util.function.Supplier;

public class CreateMovieCommandHandler implements UseCase<CreateMovieCommand, String> {
    private final MovieRepository movies;
    private final Supplier<String> idGenerator;

    public CreateMovieCommandHandler(MovieRepository movies, Supplier<String> idGenerator) {
        this.movies = movies;
        this.idGenerator = idGenerator;
    }

    @Override
    public String handle(CreateMovieCommand input) {
        if (input == null) {
            throw new ValidationException("CreateMovieCommand must not be null");
        }
        if (input.title() == null || input.title().isBlank()) {
            throw new ValidationException("Movie title must not be blank");
        }
        if (input.durationMinutes() <= 0) {
            throw new ValidationException("Movie duration must be positive");
        }
        String id = idGenerator.get();
        Movie movie;
        try {
            movie = new Movie(id, input.title(), input.description(), input.durationMinutes(),
                    input.ageRating(), MovieStatus.COMING_SOON);
        } catch (DomainException ex) {
            throw new ValidationException(ex.getMessage());
        }
        movies.save(movie);
        return movie.getId();
    }
}

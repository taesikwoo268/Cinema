package cinema.application.screenings.commands;

import java.util.Currency;
import java.util.function.Supplier;

import cinema.application.common.exceptions.ConflictException;
import cinema.application.common.exceptions.NotFoundException;
import cinema.application.common.exceptions.ValidationException;
import cinema.application.common.interfaces.HallRepository;
import cinema.application.common.interfaces.MovieRepository;
import cinema.application.common.interfaces.ScreeningRepository;
import cinema.application.common.interfaces.UseCase;
import cinema.domain.common.DomainException;
import cinema.domain.common.Money;
import cinema.domain.screening.Screening;

public final class CreateScreeningCommandHandler implements UseCase<CreateScreeningCommand, String> {

    private final ScreeningRepository screenings;
    private final MovieRepository movies;
    private final HallRepository halls;
    private final Supplier<String> idGenerator;

    public CreateScreeningCommandHandler(ScreeningRepository screenings, MovieRepository movies,
            HallRepository halls, Supplier<String> idGenerator) {
        this.screenings = screenings;
        this.movies = movies;
        this.halls = halls;
        this.idGenerator = idGenerator;
    }

    @Override
    public String handle(CreateScreeningCommand input) {
        validate(input);
        Money basePrice = createMoney(input);

        if (movies.findById(input.movieId()).isEmpty()) {
            throw new NotFoundException("Movie not found: " + input.movieId());
        }
        if (halls.findById(input.hallId()).isEmpty()) {
            throw new NotFoundException("Hall not found: " + input.hallId());
        }
        if (screenings.hasOverlap(input.hallId(), input.startTime(), input.endTime())) {
            throw new ConflictException("Screening overlaps an existing screening");
        }

        Screening screening = createScreening(input, basePrice);
        screenings.save(screening);
        return screening.getId();
    }

    private static void validate(CreateScreeningCommand input) {
        if (input == null) {
            throw new ValidationException("CreateScreeningCommand must not be null");
        }
        if (input.movieId() == null || input.movieId().isBlank()) {
            throw new ValidationException("Screening movieId must not be blank");
        }
        if (input.hallId() == null || input.hallId().isBlank()) {
            throw new ValidationException("Screening hallId must not be blank");
        }
        if (input.startTime() == null) {
            throw new ValidationException("Screening startTime must not be null");
        }
        if (input.endTime() == null) {
            throw new ValidationException("Screening endTime must not be null");
        }
        if (!input.startTime().isBefore(input.endTime())) {
            throw new ValidationException("Screening startTime must be before endTime");
        }
        if (input.basePrice() == null) {
            throw new ValidationException("Screening basePrice must not be null");
        }
        if (input.basePrice().signum() < 0) {
            throw new ValidationException("Screening basePrice must not be negative");
        }
        if (input.currency() == null || input.currency().isBlank()) {
            throw new ValidationException("Screening currency must not be blank");
        }
        try {
            Currency.getInstance(input.currency());
        } catch (IllegalArgumentException ex) {
            throw new ValidationException("Unknown currency: " + input.currency());
        }
    }

    private static Money createMoney(CreateScreeningCommand input) {
        try {
            return new Money(input.basePrice(), Currency.getInstance(input.currency()));
        } catch (DomainException | IllegalArgumentException ex) {
            throw new ValidationException(ex.getMessage());
        }
    }

    private Screening createScreening(CreateScreeningCommand input, Money basePrice) {
        try {
            return new Screening(idGenerator.get(), input.movieId(), input.hallId(), input.startTime(),
                    input.endTime(), basePrice);
        } catch (DomainException ex) {
            throw new ValidationException(ex.getMessage());
        }
    }
}

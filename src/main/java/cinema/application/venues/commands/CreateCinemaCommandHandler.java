package cinema.application.venues.commands;

import cinema.application.common.exceptions.ValidationException;
import cinema.application.common.interfaces.CinemaRepository;
import cinema.application.common.interfaces.UseCase;
import cinema.domain.cinema.Cinema;
import cinema.domain.common.DomainException;
import java.util.function.Supplier;


public class CreateCinemaCommandHandler implements UseCase<CreateCinemaCommand, String> {
    
    private final CinemaRepository cinemas;
    private final Supplier<String> idGenerator;

    public CreateCinemaCommandHandler(CinemaRepository cinemas, Supplier<String> idGenerator) {
        this.cinemas = cinemas;
        this.idGenerator = idGenerator;
    }

    @Override
    public String handle(CreateCinemaCommand input) {
        if (input == null) {
            throw new ValidationException("CreateCinemaCommand must not be null");
        }
        Cinema cinema = createCinema(input);
        cinemas.save(cinema);
        return cinema.getId();
    }

    private Cinema createCinema(CreateCinemaCommand input) {
        try {
            return new Cinema(idGenerator.get(), input.name(), input.address());
        } catch (DomainException ex) {
            throw new ValidationException(ex.getMessage());
        }
    }
}

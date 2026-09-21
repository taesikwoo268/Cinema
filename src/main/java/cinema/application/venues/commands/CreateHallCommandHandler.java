package cinema.application.venues.commands;

import cinema.application.common.exceptions.ConflictException;
import cinema.application.common.exceptions.NotFoundException;
import cinema.application.common.exceptions.ValidationException;
import cinema.application.common.interfaces.CinemaRepository;
import cinema.application.common.interfaces.HallRepository;
import cinema.application.common.interfaces.UseCase;
import cinema.domain.cinema.Hall;
import cinema.domain.common.DomainException;
import java.util.function.Supplier;


public final class CreateHallCommandHandler implements UseCase<CreateHallCommand, String> {
    private final CinemaRepository cinemas;
    private final HallRepository halls;
    private final Supplier<String> idGenerator;

    public CreateHallCommandHandler(CinemaRepository cinemas, HallRepository halls, Supplier<String> idGenerator) {
        this.cinemas = cinemas;
        this.halls = halls;
        this.idGenerator = idGenerator;
    }

    @Override
    public String handle(CreateHallCommand input) {
        if (input == null) {
            throw new ValidationException("CreateHallCommand must not be null");
        }
        String cinemaId = input.cinemaId();
        if (cinemaId == null || cinemaId.isBlank()) {
            throw new ValidationException("Hall cinemaId must not be blank");
        }
        if (input.name() == null || input.name().isBlank()) {
            throw new ValidationException("Hall name must not be blank");
        }
        if (cinemas.findById(cinemaId).isEmpty()) {
            throw new NotFoundException("Cinema not found: " + cinemaId);
        }
        Hall hall = createHall(input, cinemaId);
        if (halls.existsByCinemaIdAndName(cinemaId, hall.getName())) {
            throw new ConflictException("Hall name already used in cinema: " + hall.getName());
        }
        halls.save(hall);
        return hall.getId();
    }

    private Hall createHall(CreateHallCommand input, String cinemaId) {
        try {
            return new Hall(idGenerator.get(), cinemaId, input.name());
        } catch (DomainException ex) {
            throw new ValidationException(ex.getMessage());
        }
    }
}

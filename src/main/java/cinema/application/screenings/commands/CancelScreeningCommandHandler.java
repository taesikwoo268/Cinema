package cinema.application.screenings.commands;

import cinema.application.common.exceptions.ConflictException;
import cinema.application.common.exceptions.NotFoundException;
import cinema.application.common.exceptions.ValidationException;
import cinema.application.common.interfaces.ScreeningRepository;
import cinema.application.common.interfaces.UseCase;
import cinema.domain.common.DomainException;
import cinema.domain.screening.Screening;

public final class CancelScreeningCommandHandler implements UseCase<CancelScreeningCommand, Void> {

    private final ScreeningRepository screenings;

    public CancelScreeningCommandHandler(ScreeningRepository screenings) {
        this.screenings = screenings;
    }

    @Override
    public Void handle(CancelScreeningCommand input) {
        if (input == null) {
            throw new ValidationException("CancelScreeningCommand must not be null");
        }
        if (input.screeningId() == null || input.screeningId().isBlank()) {
            throw new ValidationException("Screening ID must not be blank");
        }

        Screening screening = screenings.findById(input.screeningId())
                .orElseThrow(() -> new NotFoundException("Screening not found: " + input.screeningId()));
        try {
            screening.cancel();
        } catch (DomainException ex) {
            throw new ConflictException(ex.getMessage());
        }
        screenings.save(screening);
        return null;
    }
}

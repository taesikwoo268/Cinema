package cinema.application.venues.commands;

import cinema.application.common.exceptions.ConflictException;
import cinema.application.common.exceptions.NotFoundException;
import cinema.application.common.exceptions.ValidationException;
import cinema.application.common.interfaces.HallRepository;
import cinema.application.common.interfaces.SeatRepository;
import cinema.application.common.interfaces.UseCase;
import cinema.domain.cinema.Seat;
import cinema.domain.cinema.SeatType;
import cinema.domain.common.DomainException;
import java.util.function.Supplier;

public final class CreateSeatCommandHandler implements UseCase<CreateSeatCommand, String> {
    private final HallRepository halls;
    private final SeatRepository seats;
    private final Supplier<String> idGenerator;

    public CreateSeatCommandHandler(HallRepository halls, SeatRepository seats, Supplier<String> idGenerator) {
        this.halls = halls;
        this.seats = seats;
        this.idGenerator = idGenerator;
    }

    @Override
    public String handle(CreateSeatCommand input) {
        if (input == null) {
            throw new ValidationException("CreateSeatCommand must not be null");
        }
        String hallId = input.hallId();
        if (hallId == null || hallId.isBlank()) {
            throw new ValidationException("Seat hallId must not be blank");
        }
        SeatType type = parseType(input.type());
        if (halls.findById(hallId).isEmpty()) {
            throw new NotFoundException("Hall not found: " + hallId);
        }
        Seat seat = createSeat(input, type, hallId);
        if (seats.existsByHallIdAndRowAndNumber(hallId, seat.getRow(), seat.getNumber())) {
            throw new ConflictException(
                "Seat position already used in hall: " + seat.getRow() + seat.getNumber());
        }
        seats.save(seat);
        return seat.getId();
    }

    private static SeatType parseType(String type) {
        if (type == null || type.isBlank()) {
            throw new ValidationException("Seat type must not be blank");
        }
        try {
            return SeatType.valueOf(type);
        } catch (IllegalArgumentException ex) {
            throw new ValidationException("Unknown seat type: " + type);
        }
    }

    private Seat createSeat(CreateSeatCommand input, SeatType type, String hallId) {
        Integer number = input.number();
        if (number == null) {
            throw new ValidationException("Seat number must not be null");
        }
        try {
            return new Seat(idGenerator.get(), hallId, input.row(), number, type);
        } catch (DomainException ex) {
            throw new ValidationException(ex.getMessage());
        }
    }
}

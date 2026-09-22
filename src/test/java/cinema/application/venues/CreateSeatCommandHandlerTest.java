package cinema.application.venues;

import cinema.application.common.exceptions.ConflictException;
import cinema.application.common.exceptions.NotFoundException;
import cinema.application.common.exceptions.ValidationException;
import cinema.application.common.interfaces.HallRepository;
import cinema.application.common.interfaces.SeatRepository;
import cinema.application.venues.commands.CreateSeatCommand;
import cinema.application.venues.commands.CreateSeatCommandHandler;
import cinema.domain.cinema.Hall;
import cinema.domain.cinema.Seat;
import cinema.domain.cinema.SeatType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CreateSeatCommandHandlerTest {

    @Test
    void createsSeatForExistingHall() {

        Hall hall = new Hall("h-123", "c-123", "Hall-123");

        HallRepository halls = mock(HallRepository.class);
        SeatRepository seats = mock(SeatRepository.class);
        Supplier<String> idGenerator = () -> "s-123";

        CreateSeatCommandHandler handler = new CreateSeatCommandHandler(halls, seats, idGenerator);

        CreateSeatCommand input = new CreateSeatCommand("h-123", "A", 1, "NORMAL");

        when(halls.findById("h-123")).thenReturn(Optional.of(hall));

        String result = handler.handle(input);

        assertEquals("s-123", result);

        ArgumentCaptor<Seat> captor = ArgumentCaptor.forClass(Seat.class);

        verify(seats).save(captor.capture());

        Seat savedSeat = captor.getValue();

        assertEquals("h-123", savedSeat.getHallId());
        assertEquals("A", savedSeat.getRow());
        assertEquals(1, savedSeat.getNumber());
        assertEquals(SeatType.NORMAL, savedSeat.getType());

    }

    @Test
    void rejectsNullCommand() {

        HallRepository halls = mock(HallRepository.class);
        SeatRepository seats = mock(SeatRepository.class);
        Supplier<String> idGenerator = () -> "s-123";

        CreateSeatCommandHandler handler = new CreateSeatCommandHandler(halls, seats, idGenerator);

        assertThrows(ValidationException.class, () -> handler.handle(null));

    }

    @Test
    void missingNumber() {

        Hall hall = new Hall("h-123", "c-123", "Hall-123");


        HallRepository halls = mock(HallRepository.class);
        SeatRepository seats = mock(SeatRepository.class);
        Supplier<String> idGenerator = () -> "s-123";

        CreateSeatCommandHandler handler = new CreateSeatCommandHandler(halls, seats, idGenerator);

        CreateSeatCommand input = new CreateSeatCommand("h-123", "A", null, "NORMAL");

        when(halls.findById("h-123")).thenReturn(Optional.of(hall));

        assertThrows(ValidationException.class, () -> handler.handle(input));

    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0})
    void invalidNumber(int seatNum) {

        Hall hall = new Hall("h-123", "c-123", "Hall-123");


        HallRepository halls = mock(HallRepository.class);
        SeatRepository seats = mock(SeatRepository.class);
        Supplier<String> idGenerator = () -> "s-123";

        CreateSeatCommandHandler handler = new CreateSeatCommandHandler(halls, seats, idGenerator);

        CreateSeatCommand input = new CreateSeatCommand("h-123", "A", seatNum, "NORMAL");

        when(halls.findById("h-123")).thenReturn(Optional.of(hall));

        assertThrows(ValidationException.class, () -> handler.handle(input));

    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"SPECIAL"})
    void missingOrUnknownType(String seatType) {

        Hall hall = new Hall("h-123", "c-123", "Hall-123");


        HallRepository halls = mock(HallRepository.class);
        SeatRepository seats = mock(SeatRepository.class);
        Supplier<String> idGenerator = () -> "s-123";

        CreateSeatCommandHandler handler = new CreateSeatCommandHandler(halls, seats, idGenerator);

        CreateSeatCommand input = new CreateSeatCommand("h-123", "A", 1, seatType);

        when(halls.findById("h-123")).thenReturn(Optional.of(hall));

        assertThrows(ValidationException.class, () -> handler.handle(input));

    }

    @Test
    void missingHallDoesNotSave() {

        HallRepository halls = mock(HallRepository.class);
        SeatRepository seats = mock(SeatRepository.class);
        Supplier<String> idGenerator = () -> "s-123";

        CreateSeatCommandHandler handler = new CreateSeatCommandHandler(halls, seats, idGenerator);

        CreateSeatCommand input = new CreateSeatCommand("h-123", "A", 1, "NORMAL");

        when(halls.findById("h-123")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> handler.handle(input));

        verify(seats, never()).save(any(Seat.class));
    }

    @Test
    void duplicatePositionDoesNotSave() {

        Hall hall = new Hall("h-123", "c-123", "Hall-123");


        HallRepository halls = mock(HallRepository.class);
        SeatRepository seats = mock(SeatRepository.class);
        Supplier<String> idGenerator = () -> "s-123";

        CreateSeatCommandHandler handler = new CreateSeatCommandHandler(halls, seats, idGenerator);

        CreateSeatCommand input = new CreateSeatCommand("h-123", "A", 1, "NORMAL");

        when(halls.findById("h-123")).thenReturn(Optional.of(hall));

        when(seats.existsByHallIdAndRowAndNumber("h-123", "A", 1)).thenReturn(true);

        assertThrows(ConflictException.class, () -> handler.handle(input));

        verify(seats, never()).save(any(Seat.class));

    }

    @Test
    void checksDuplicateUsingNormalizedRow() {

        Hall hall = new Hall("h-123", "c-123", "Hall-123");

        HallRepository halls = mock(HallRepository.class);
        SeatRepository seats = mock(SeatRepository.class);
        Supplier<String> idGenerator = () -> "s-123";

        CreateSeatCommandHandler handler = new CreateSeatCommandHandler(halls, seats, idGenerator);

        CreateSeatCommand input = new CreateSeatCommand("h-123", "a", 1, "NORMAL");
    /*
        assertTrue(seats.existsByHallIdAndRowAndNumber("h-123", "A", 1));
    */
        when(halls.findById("h-123")).thenReturn(Optional.of(hall));

        when(seats.existsByHallIdAndRowAndNumber("h-123", "A", 1)).thenReturn(true);

        assertThrows(ConflictException.class, () -> handler.handle(input));

        verify(seats).existsByHallIdAndRowAndNumber("h-123", "A", 1);
        verify(seats, never()).save(any(Seat.class));

    }

    @Test
    void repositoryConflictPropagates() {

        Hall hall = new Hall("h-123", "c-123", "Hall-123");


        HallRepository halls = mock(HallRepository.class);
        SeatRepository seats = mock(SeatRepository.class);
        Supplier<String> idGenerator = () -> "s-123";

        CreateSeatCommandHandler handler = new CreateSeatCommandHandler(halls, seats, idGenerator);

        CreateSeatCommand input = new CreateSeatCommand("h-123", "A", 1, "NORMAL");

        when(halls.findById("h-123")).thenReturn(Optional.of(hall));

        when(seats.existsByHallIdAndRowAndNumber("h-123", "A", 1)).thenReturn(false);

        doThrow(new ConflictException("Seat position already used in hall: " + "A" + "1"))
                .when(seats).save(any(Seat.class));

        assertThrows(ConflictException.class, () -> handler.handle(input));

    }

    @Test
    void domainValidationBecomesApplicationValidation() {
        Hall hall = new Hall("h-123", "c-123", "Hall-123");


        HallRepository halls = mock(HallRepository.class);
        SeatRepository seats = mock(SeatRepository.class);
        Supplier<String> idGenerator = () -> "s-123";

        CreateSeatCommandHandler handler = new CreateSeatCommandHandler(halls, seats, idGenerator);

        CreateSeatCommand input = new CreateSeatCommand("h-123", null, 1, "NORMAL");

        when(halls.findById("h-123")).thenReturn(Optional.of(hall));

        assertThrows(ValidationException.class, () -> handler.handle(input));
    }

}

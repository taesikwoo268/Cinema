package cinema.application.venues;

import cinema.application.common.exceptions.ConflictException;
import cinema.application.common.exceptions.NotFoundException;
import cinema.application.common.exceptions.ValidationException;
import cinema.application.common.interfaces.CinemaRepository;
import cinema.application.common.interfaces.HallRepository;
import cinema.application.venues.commands.CreateHallCommand;
import cinema.application.venues.commands.CreateHallCommandHandler;
import cinema.domain.cinema.Cinema;
import cinema.domain.cinema.Hall;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.context.TestExecutionListeners;

import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class CreateHallCommandHandlerTest {
    @Test
    void createsHallForExistingCinema() {

        Cinema cinema = new Cinema("c-123", "Cinema-123", "Ha Noi");

        CinemaRepository cinemas = mock(CinemaRepository.class);
        HallRepository halls = mock(HallRepository.class);
        Supplier<String> idGenerator = () -> "h-123";

        CreateHallCommandHandler handler = new CreateHallCommandHandler(cinemas, halls, idGenerator);

        CreateHallCommand input = new CreateHallCommand("c-123", "Hall-123");

        when(cinemas.findById("c-123")).thenReturn(Optional.of(cinema));

        String result = handler.handle(input);

        assertEquals("h-123", result);

        ArgumentCaptor<Hall> captor = ArgumentCaptor.forClass(Hall.class);

        verify(halls).save(captor.capture());

        Hall savedHall = captor.getValue();

        assertEquals("h-123", savedHall.getId());
        assertEquals("Hall-123", savedHall.getName());
        assertEquals("c-123", savedHall.getCinemaId());

    }

    @Test
    void rejectsNullCommandAndBlankParentId() {

        CinemaRepository cinemas = mock(CinemaRepository.class);
        HallRepository halls = mock(HallRepository.class);
        Supplier<String> idGenerator = () -> "h-123";

        CreateHallCommandHandler handler = new CreateHallCommandHandler(cinemas, halls, idGenerator);

        CreateHallCommand input = new CreateHallCommand("","Hall-123");

        assertThrows(ValidationException.class,
                () -> handler.handle(null));

        assertThrows(ValidationException.class,
                () -> handler.handle(input));

    }

    @Test
    void missingCinemaDoesNotSave() {

        Cinema cinema = new Cinema("c-123", "Cinema-123", "Ha Noi");

        CinemaRepository cinemas = mock(CinemaRepository.class);
        HallRepository halls = mock(HallRepository.class);
        Supplier<String> idGenerator = () -> "h-123";

        CreateHallCommandHandler handler = new CreateHallCommandHandler(cinemas, halls, idGenerator);

        CreateHallCommand input = new CreateHallCommand("c-123", "Hall-123");

        when(cinemas.findById("c-123")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> handler.handle(input));

        verify(halls, never()).save(any(Hall.class));

    }

    @Test
    void duplicateNameDoesNotSave() {

        Cinema cinema = new Cinema("c-123", "Cinema-123", "Ha Noi");

        CinemaRepository cinemas = mock(CinemaRepository.class);
        HallRepository halls = mock(HallRepository.class);
        Supplier<String> idGenerator = () -> "h-123";

        CreateHallCommandHandler handler = new CreateHallCommandHandler(cinemas, halls, idGenerator);

        CreateHallCommand input = new CreateHallCommand("c-123", "Hall-123");

        when(cinemas.findById("c-123")).thenReturn(Optional.of(cinema));

        when(halls.existsByCinemaIdAndName("c-123", "Hall-123"))
                .thenReturn(true);

        assertThrows(ConflictException.class, () -> handler.handle(input));

        verify(halls, never()).save(any(Hall.class));

    }

    @Test
    void repositoryConflictPropagates() {

        Cinema cinema = new Cinema("c-123", "Cinema-123", "Ha Noi");

        CinemaRepository cinemas = mock(CinemaRepository.class);
        HallRepository halls = mock(HallRepository.class);
        Supplier<String> idGenerator = () -> "h-123";

        CreateHallCommandHandler handler = new CreateHallCommandHandler(cinemas, halls, idGenerator);

        CreateHallCommand input = new CreateHallCommand("c-123", "Hall-123");

        when(cinemas.findById("c-123")).thenReturn(Optional.of(cinema));

        when(halls.existsByCinemaIdAndName("c-123", "Hall-123"))
                .thenReturn(false);

        doThrow(new ConflictException("Hall name already used in cinema: " + "Hall-123"))
                .when(halls).save(any(Hall.class));

        assertThrows(ConflictException.class, () -> handler.handle(input));

    }

    @Test
    void domainValidationBecomesApplicationValidation() {
        Cinema cinema = new Cinema("c-123", "Cinema-123", "Ha Noi");

        CinemaRepository cinemas = mock(CinemaRepository.class);
        HallRepository halls = mock(HallRepository.class);
        Supplier<String> idGenerator = () -> "h-123";

        CreateHallCommandHandler handler = new CreateHallCommandHandler(cinemas, halls, idGenerator);

        CreateHallCommand input = new CreateHallCommand("c-123", "");

        when(cinemas.findById("c-123")).thenReturn(Optional.of(cinema));

        assertThrows(ValidationException.class, () -> handler.handle(input));
    }
}

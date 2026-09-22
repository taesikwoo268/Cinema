package cinema.application.venues;

import cinema.application.common.exceptions.ConflictException;
import cinema.application.common.exceptions.ValidationException;
import cinema.application.common.interfaces.CinemaRepository;
import cinema.application.venues.commands.CreateCinemaCommand;
import cinema.application.venues.commands.CreateCinemaCommandHandler;
import cinema.domain.cinema.Cinema;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class CreateCinemaCommandHandlerTest {

    @Test
    void createsCinemaAndReturnsGeneratedId() {

        CinemaRepository cinemas = mock(CinemaRepository.class);
        Supplier<String> idGenerator = () -> "c-123";
        CreateCinemaCommandHandler handler = new CreateCinemaCommandHandler(cinemas, idGenerator);

        CreateCinemaCommand input = new CreateCinemaCommand("Cinema-01", "Ha Noi");

        String result = handler.handle(input);
        assertEquals("c-123", result);

        ArgumentCaptor<Cinema> captor = ArgumentCaptor.forClass(Cinema.class);

        verify(cinemas).save(captor.capture());
        Cinema savedCinema = captor.getValue();

        assertEquals("c-123", savedCinema.getId());
        assertEquals("Cinema-01", savedCinema.getName());
        assertEquals("Ha Noi", savedCinema.getAddress());

    }

    @Test
    void rejectsNullCommand() {

        CinemaRepository cinemas = mock(CinemaRepository.class);
        Supplier<String> idGenerator = () -> "c-123";
        CreateCinemaCommandHandler handler = new CreateCinemaCommandHandler(cinemas, idGenerator);

        assertThrows(ValidationException.class, () -> handler.handle(null));

    }

    @Test
    void invalidDomainDataBecomesValidationException() {

        CinemaRepository cinemas = mock(CinemaRepository.class);
        Supplier<String> idGenerator = () -> "c-123";
        CreateCinemaCommandHandler handler = new CreateCinemaCommandHandler(cinemas, idGenerator);
        CreateCinemaCommand input = new CreateCinemaCommand("", "Ha Noi");

        assertThrows(ValidationException.class,
                () -> handler.handle(input));

    }

    @Test
    void duplicateIdConflictPropagates() {

        CinemaRepository cinemas = mock(CinemaRepository.class);
        Supplier<String> idGenerator = () -> "c-123";
        CreateCinemaCommandHandler handler = new CreateCinemaCommandHandler(cinemas, idGenerator);
        CreateCinemaCommand input = new CreateCinemaCommand("Cinema-01", "Ha Noi");

        doThrow(new ConflictException("Cinema ID already exists")).
                when(cinemas).save(any(Cinema.class));

        assertThrows(ConflictException.class, () -> handler.handle(input));
    }


}

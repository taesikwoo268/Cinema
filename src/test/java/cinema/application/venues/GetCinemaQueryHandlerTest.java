package cinema.application.venues;

import cinema.application.common.exceptions.NotFoundException;
import cinema.application.common.exceptions.ValidationException;
import cinema.application.common.interfaces.CinemaRepository;
import cinema.application.venues.models.CinemaView;
import cinema.application.venues.queries.GetCinemaQuery;
import cinema.application.venues.queries.GetCinemaQueryHandler;
import cinema.domain.cinema.Cinema;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GetCinemaQueryHandlerTest {

    @Test
    void returnsView() {

        Cinema cinema = new Cinema("c-1", "Cinema-1", "Ha Noi");

        CinemaRepository cinemas = mock(CinemaRepository.class);

        GetCinemaQueryHandler handler = new GetCinemaQueryHandler(cinemas);

        GetCinemaQuery input = new GetCinemaQuery("c-1");

        when(cinemas.findById("c-1")).thenReturn(Optional.of(cinema));

        CinemaView result = handler.handle(input);

        assertEquals("c-1", result.id());
        assertEquals("Cinema-1", result.name());
        assertEquals("Ha Noi", result.address());

    }

    @Test
    void missingCinemaThrowsNotFound() {

        CinemaRepository cinemas = mock(CinemaRepository.class);

        GetCinemaQueryHandler handler = new GetCinemaQueryHandler(cinemas);

        GetCinemaQuery input = new GetCinemaQuery("c-1");

        when(cinemas.findById("c-1")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> handler.handle(input));

    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void rejectsBlankId(String id) {

        CinemaRepository cinemas = mock(CinemaRepository.class);

        GetCinemaQueryHandler handler = new GetCinemaQueryHandler(cinemas);

        GetCinemaQuery input = new GetCinemaQuery(id);

        assertThrows(ValidationException.class,
                () -> handler.handle(input));

    }
}

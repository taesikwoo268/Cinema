package cinema.application.venues;

import cinema.application.common.exceptions.ValidationException;
import cinema.application.common.interfaces.CinemaRepository;
import cinema.application.venues.models.CinemaView;
import cinema.application.venues.queries.GetCinemasQuery;
import cinema.application.venues.queries.GetCinemasQueryHandler;
import cinema.domain.cinema.Cinema;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GetCinemasQueryHandlerTest {

    @Test
    void returnsMappedList() {

        Cinema cinema1 = new Cinema("c-1", "Cinema-1", "Ha Noi");
        Cinema cinema2 = new Cinema("c-2", "Cinema-2", "Hue");

        CinemaRepository cinemas = mock(CinemaRepository.class);

        GetCinemasQueryHandler handler = new GetCinemasQueryHandler(cinemas);

        when(cinemas.findAll()).thenReturn(List.of(cinema1, cinema2));

        List<CinemaView> result =
                handler.handle(new GetCinemasQuery());

        assertEquals(2, result.size());

        assertEquals("c-1", result.getFirst().id());
        assertEquals("Cinema-1", result.getFirst().name());
        assertEquals("Ha Noi", result.getFirst().address());

        assertEquals("c-2", result.get(1).id());
        assertEquals("Cinema-2", result.get(1).name());
        assertEquals("Hue", result.get(1).address());

    }

    @Test
    void returnsEmptyList() {

        CinemaRepository cinemas = mock(CinemaRepository.class);

        GetCinemasQueryHandler handler = new GetCinemasQueryHandler(cinemas);

        when(cinemas.findAll()).thenReturn(List.of());

        List<CinemaView> result =
                handler.handle(new GetCinemasQuery());

        assertTrue(result.isEmpty());

    }

    @Test
    void rejectsNullQuery() {

        CinemaRepository cinemas = mock(CinemaRepository.class);

        GetCinemasQueryHandler handler = new GetCinemasQueryHandler(cinemas);

        when(cinemas.findAll()).thenReturn(List.of());

        assertThrows(ValidationException.class, () -> handler.handle(null));

    }
}

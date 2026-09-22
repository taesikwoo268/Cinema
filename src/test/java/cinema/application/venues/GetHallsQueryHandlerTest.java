package cinema.application.venues;

import cinema.application.common.exceptions.NotFoundException;
import cinema.application.common.exceptions.ValidationException;
import cinema.application.common.interfaces.CinemaRepository;
import cinema.application.common.interfaces.HallRepository;
import cinema.application.venues.models.HallView;
import cinema.application.venues.queries.GetHallsQuery;
import cinema.application.venues.queries.GetHallsQueryHandler;
import cinema.domain.cinema.Cinema;
import cinema.domain.cinema.Hall;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GetHallsQueryHandlerTest {

    @Test
    void returnsOnlyRequestedCinemaHalls() {

        Cinema cinema = new Cinema("c-1", "Cinema-1", "Ha Noi");

        Hall hall1 = new Hall("h-1", "c-1", "Hall-1");
        Hall hall2 = new Hall("h-2", "c-1", "Hall-2");

        CinemaRepository cinemas = mock(CinemaRepository.class);

        HallRepository halls = mock(HallRepository.class);

        GetHallsQueryHandler handler = new GetHallsQueryHandler(cinemas, halls);

        GetHallsQuery input = new GetHallsQuery("c-1");

        when(cinemas.findById("c-1")).thenReturn(Optional.of(cinema));

        when(halls.findByCinemaId("c-1")).thenReturn(List.of(hall1, hall2));

        List<HallView> result = handler.handle(input);

        assertEquals(2, result.size());

        assertEquals("h-1", result.getFirst().id());
        assertEquals("c-1", result.getFirst().cinemaId());
        assertEquals("Hall-1", result.getFirst().name());

        assertEquals("h-2", result.get(1).id());
        assertEquals("c-1", result.get(1).cinemaId());
        assertEquals("Hall-2", result.get(1).name());
    }

    @Test
    void existingCinemaWithoutHallsReturnsEmpty() {

        Cinema cinema = new Cinema("c-1", "Cinema-1", "Ha Noi");

        CinemaRepository cinemas = mock(CinemaRepository.class);

        HallRepository halls = mock(HallRepository.class);

        GetHallsQueryHandler handler = new GetHallsQueryHandler(cinemas, halls);

        GetHallsQuery input = new GetHallsQuery("c-1");

        when(cinemas.findById("c-1")).thenReturn(Optional.of(cinema));

        when(halls.findByCinemaId("c-1")).thenReturn(List.of());

        List<HallView> result = handler.handle(input);

        assertTrue(result.isEmpty());
    }

    @Test
    void missingCinemaThrowsNotFound() {

        CinemaRepository cinemas = mock(CinemaRepository.class);

        HallRepository halls = mock(HallRepository.class);

        GetHallsQueryHandler handler = new GetHallsQueryHandler(cinemas, halls);

        GetHallsQuery input = new GetHallsQuery("c-1");

        when(cinemas.findById("c-1")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> handler.handle(input));

    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void rejectsBlankCinemaId(String cinemaId) {

        CinemaRepository cinemas = mock(CinemaRepository.class);

        HallRepository halls = mock(HallRepository.class);

        GetHallsQueryHandler handler = new GetHallsQueryHandler(cinemas, halls);

        GetHallsQuery input = new GetHallsQuery(cinemaId);

        assertThrows(ValidationException.class, () -> handler.handle(input));

    }
}

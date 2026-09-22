package cinema.application.venues;

import cinema.application.common.exceptions.NotFoundException;
import cinema.application.common.exceptions.ValidationException;
import cinema.application.common.interfaces.CinemaRepository;
import cinema.application.common.interfaces.HallRepository;
import cinema.application.venues.models.CinemaView;
import cinema.application.venues.models.HallView;
import cinema.application.venues.queries.GetCinemaQuery;
import cinema.application.venues.queries.GetCinemaQueryHandler;
import cinema.application.venues.queries.GetHallQuery;
import cinema.application.venues.queries.GetHallQueryHandler;
import cinema.domain.cinema.Cinema;
import cinema.domain.cinema.Hall;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GetHallQueryHandlerTest {

    @Test
    void returnsView() {

        Hall hall = new Hall("h-1", "c-1", "Hall-1");

        HallRepository halls = mock(HallRepository.class);

        GetHallQueryHandler handler = new GetHallQueryHandler(halls);

        GetHallQuery input = new GetHallQuery("h-1");

        when(halls.findById("h-1")).thenReturn(Optional.of(hall));

        HallView result = handler.handle(input);

        assertEquals("h-1", result.id());
        assertEquals("c-1", result.cinemaId());
        assertEquals("Hall-1", result.name());

    }

    @Test
    void missingHallThrowsNotFound() {

        HallRepository halls = mock(HallRepository.class);

        GetHallQueryHandler handler = new GetHallQueryHandler(halls);

        GetHallQuery input = new GetHallQuery("h-1");

        when(halls.findById("h-1")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> handler.handle(input));

    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void rejectsBlankId(String id) {

        HallRepository halls = mock(HallRepository.class);

        GetHallQueryHandler handler = new GetHallQueryHandler(halls);

        GetHallQuery input = new GetHallQuery(id);

        assertThrows(ValidationException.class,
                () -> handler.handle(input));

    }
}

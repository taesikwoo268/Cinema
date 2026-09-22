package cinema.application.venues;

import cinema.application.common.exceptions.NotFoundException;
import cinema.application.common.exceptions.ValidationException;
import cinema.application.common.interfaces.CinemaRepository;
import cinema.application.common.interfaces.HallRepository;
import cinema.application.common.interfaces.SeatRepository;
import cinema.application.common.models.SeatView;
import cinema.application.venues.models.PhysicalSeatView;
import cinema.application.venues.queries.GetHallsQuery;
import cinema.application.venues.queries.GetHallsQueryHandler;
import cinema.application.venues.queries.GetSeatsQuery;
import cinema.application.venues.queries.GetSeatsQueryHandler;
import cinema.domain.cinema.Hall;
import cinema.domain.cinema.Seat;
import cinema.domain.cinema.SeatType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GetSeatsQueryHandlerTest {

    @Test
    void returnsPhysicalSeatViews() {

        Hall hall = new Hall("h-1", "c-1", "Hall-1");

        Seat seat1 = new Seat("s-1", "h-1", "A", 1, SeatType.NORMAL);
        Seat seat2 = new Seat("s-2", "h-1", "a", 2, SeatType.VIP);

        HallRepository halls = mock(HallRepository.class);
        SeatRepository seats = mock(SeatRepository.class);

        GetSeatsQueryHandler handler = new GetSeatsQueryHandler(halls, seats);

        GetSeatsQuery input = new GetSeatsQuery("h-1");

        when(halls.findById("h-1")).thenReturn(Optional.of(hall));

        when(seats.findByHallId("h-1")).thenReturn(List.of(seat1, seat2));

        List<PhysicalSeatView> result = handler.handle(input);

        assertEquals(2, result.size());

        assertEquals("s-1", result.getFirst().id());
        assertEquals("h-1", result.getFirst().hallId());
        assertEquals("A", result.getFirst().row());
        assertEquals(1, result.getFirst().number());
        assertEquals("NORMAL", result.getFirst().type());

        assertEquals("s-2", result.get(1).id());
        assertEquals("h-1", result.get(1).hallId());
        assertEquals("A", result.get(1).row());
        assertEquals(2, result.get(1).number());
        assertEquals("VIP", result.get(1).type());

    }

    @Test
    void existingHallWithoutSeatsReturnsEmpty() {

        Hall hall = new Hall("h-1", "c-1", "Hall-1");

        HallRepository halls = mock(HallRepository.class);
        SeatRepository seats = mock(SeatRepository.class);

        GetSeatsQueryHandler handler = new GetSeatsQueryHandler(halls, seats);

        GetSeatsQuery input = new GetSeatsQuery("h-1");

        when(halls.findById("h-1")).thenReturn(Optional.of(hall));

        when(seats.findByHallId("h-1")).thenReturn(List.of());

        List<PhysicalSeatView> result = handler.handle(input);

        assertTrue(result.isEmpty());

    }

    @Test
    void missingHallThrowsNotFound() {

        HallRepository halls = mock(HallRepository.class);
        SeatRepository seats = mock(SeatRepository.class);

        GetSeatsQueryHandler handler = new GetSeatsQueryHandler(halls, seats);

        GetSeatsQuery input = new GetSeatsQuery("h-1");

        when(halls.findById("h-1")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> handler.handle(input));

    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void rejectsBlankCinemaId(String hallId) {

        HallRepository halls = mock(HallRepository.class);

        SeatRepository seats = mock(SeatRepository.class);

        GetSeatsQueryHandler handler = new GetSeatsQueryHandler(halls, seats);

        GetSeatsQuery input = new GetSeatsQuery(hallId);

        assertThrows(ValidationException.class, () -> handler.handle(input));

    }

}

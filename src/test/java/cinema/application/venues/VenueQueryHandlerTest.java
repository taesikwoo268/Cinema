package cinema.application.venues;

import cinema.application.common.exceptions.*;
import cinema.application.common.interfaces.*;
import cinema.application.venues.models.*;
import cinema.application.venues.queries.*;
import cinema.domain.cinema.*;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VenueQueryHandlerTest {
    private final CinemaRepository cinemas = mock(CinemaRepository.class);
    private final HallRepository halls = mock(HallRepository.class);
    private final SeatRepository seats = mock(SeatRepository.class);
    private final GetCinemasQueryHandler getCinemas = new GetCinemasQueryHandler(cinemas);
    private final GetCinemaQueryHandler getCinema = new GetCinemaQueryHandler(cinemas);
    private final GetHallsQueryHandler getHalls = new GetHallsQueryHandler(cinemas, halls);
    private final GetHallQueryHandler getHall = new GetHallQueryHandler(halls);
    private final GetSeatsQueryHandler getSeats = new GetSeatsQueryHandler(halls, seats);

    @Test
    void mapsAllCinemaFieldsForMultipleResults() {
        when(cinemas.findAll()).thenReturn(List.of(new Cinema("c1", "First", "North"), new Cinema("c2", "Second", "South")));
        assertEquals(List.of(new CinemaView("c1", "First", "North"), new CinemaView("c2", "Second", "South")),
                getCinemas.handle(new GetCinemasQuery()));
    }

    @Test
    void emptyCinemaRepositoryReturnsEmptyList() {
        assertEquals(List.of(), getCinemas.handle(new GetCinemasQuery()));
    }

    @Test
    void singleLookupsPreserveExactIdsAndMapAllFields() {
        when(cinemas.findById(" c1 ")).thenReturn(Optional.of(new Cinema(" c1 ", "Cinema", "Address")));
        when(halls.findById(" h1 ")).thenReturn(Optional.of(new Hall(" h1 ", " c1 ", "Hall")));
        assertEquals(new CinemaView(" c1 ", "Cinema", "Address"), getCinema.handle(new GetCinemaQuery(" c1 ")));
        assertEquals(new HallView(" h1 ", " c1 ", "Hall"), getHall.handle(new GetHallQuery(" h1 ")));
    }

    @Test
    void childQueriesMapAllFieldsForRequestedParent() {
        when(cinemas.findById(" c1 ")).thenReturn(Optional.of(new Cinema(" c1 ", "Cinema", "Address")));
        when(halls.findByCinemaId(" c1 ")).thenReturn(List.of(new Hall("h1", " c1 ", "First"), new Hall("h2", " c1 ", "Second")));
        when(halls.findById(" h1 ")).thenReturn(Optional.of(new Hall(" h1 ", "c2", "Hall")));
        when(seats.findByHallId(" h1 ")).thenReturn(List.of(new Seat("s1", " h1 ", "A", 2, SeatType.NORMAL),
                new Seat("s2", " h1 ", "B", 3, SeatType.COUPLE)));
        assertEquals(List.of(new HallView("h1", " c1 ", "First"), new HallView("h2", " c1 ", "Second")),
                getHalls.handle(new GetHallsQuery(" c1 ")));
        assertEquals(List.of(new PhysicalSeatView("s1", " h1 ", "A", 2, "NORMAL"),
                new PhysicalSeatView("s2", " h1 ", "B", 3, "COUPLE")), getSeats.handle(new GetSeatsQuery(" h1 ")));
    }

    @Test
    void existingParentsWithoutChildrenReturnEmptyLists() {
        when(cinemas.findById("c1")).thenReturn(Optional.of(new Cinema("c1", "Cinema", "Address")));
        when(halls.findById("h1")).thenReturn(Optional.of(new Hall("h1", "c1", "Hall")));
        assertEquals(List.of(), getHalls.handle(new GetHallsQuery("c1")));
        assertEquals(List.of(), getSeats.handle(new GetSeatsQuery("h1")));
    }

    @Test
    void missingResourcesThrowNotFound() {
        assertThrows(NotFoundException.class, () -> getCinema.handle(new GetCinemaQuery("missing")));
        assertThrows(NotFoundException.class, () -> getHall.handle(new GetHallQuery("missing")));
        assertThrows(NotFoundException.class, () -> getHalls.handle(new GetHallsQuery("missing")));
        assertThrows(NotFoundException.class, () -> getSeats.handle(new GetSeatsQuery("missing")));
        verify(halls, never()).findByCinemaId(any());
        verifyNoInteractions(seats);
    }

    @Test
    void rejectsNullQueriesWithoutRepositoryAccess() {
        assertThrows(ValidationException.class, () -> getCinemas.handle(null));
        assertThrows(ValidationException.class, () -> getCinema.handle(null));
        assertThrows(ValidationException.class, () -> getHalls.handle(null));
        assertThrows(ValidationException.class, () -> getHall.handle(null));
        assertThrows(ValidationException.class, () -> getSeats.handle(null));
        verifyNoInteractions(cinemas, halls, seats);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t\n"})
    void rejectsBlankIdsWithoutRepositoryAccess(String id) {
        assertThrows(ValidationException.class, () -> getCinema.handle(new GetCinemaQuery(id)));
        assertThrows(ValidationException.class, () -> getHalls.handle(new GetHallsQuery(id)));
        assertThrows(ValidationException.class, () -> getHall.handle(new GetHallQuery(id)));
        assertThrows(ValidationException.class, () -> getSeats.handle(new GetSeatsQuery(id)));
        verifyNoInteractions(cinemas, halls, seats);
    }
}

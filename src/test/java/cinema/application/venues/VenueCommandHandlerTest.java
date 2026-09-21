package cinema.application.venues;

import cinema.application.common.exceptions.*;
import cinema.application.common.interfaces.*;
import cinema.application.venues.commands.*;
import cinema.domain.cinema.*;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VenueCommandHandlerTest {
    private final CinemaRepository cinemas = mock(CinemaRepository.class);
    private final HallRepository halls = mock(HallRepository.class);
    private final SeatRepository seats = mock(SeatRepository.class);
    private final CreateCinemaCommandHandler createCinema = new CreateCinemaCommandHandler(cinemas, () -> "c1");
    private final CreateHallCommandHandler createHall = new CreateHallCommandHandler(cinemas, halls, () -> "h1");
    private final CreateSeatCommandHandler createSeat = new CreateSeatCommandHandler(halls, seats, () -> "s1");

    private void existingParents() {
        when(cinemas.findById(" c1 ")).thenReturn(Optional.of(new Cinema(" c1 ", "Cinema", "Address")));
        when(halls.findById(" h1 ")).thenReturn(Optional.of(new Hall(" h1 ", "c1", "Hall")));
    }

    @Test
    void createsNormalizedCinemaWithGeneratedId() {
        assertEquals("c1", createCinema.handle(new CreateCinemaCommand(" Cinema ", " Address ")));
        var saved = ArgumentCaptor.forClass(Cinema.class);
        verify(cinemas).save(saved.capture());
        assertAll(() -> assertEquals("c1", saved.getValue().getId()),
                () -> assertEquals("Cinema", saved.getValue().getName()),
                () -> assertEquals("Address", saved.getValue().getAddress()));
    }

    @Test
    void createsHallPreservingParentIdAndNormalizingName() {
        existingParents();
        assertEquals("h1", createHall.handle(new CreateHallCommand(" c1 ", " Hall ")));
        var saved = ArgumentCaptor.forClass(Hall.class);
        verify(halls).save(saved.capture());
        assertAll(() -> assertEquals("h1", saved.getValue().getId()),
                () -> assertEquals(" c1 ", saved.getValue().getCinemaId()),
                () -> assertEquals("Hall", saved.getValue().getName()));
        verify(halls).existsByCinemaIdAndName(" c1 ", "Hall");
    }

    @Test
    void createsOneCoupleSeatAndChecksNormalizedPosition() {
        existingParents();
        assertEquals("s1", createSeat.handle(new CreateSeatCommand(" h1 ", " a ", 2, "COUPLE")));
        var saved = ArgumentCaptor.forClass(Seat.class);
        verify(seats).save(saved.capture());
        assertAll(() -> assertEquals("s1", saved.getValue().getId()),
                () -> assertEquals(" h1 ", saved.getValue().getHallId()),
                () -> assertEquals("A", saved.getValue().getRow()),
                () -> assertEquals(2, saved.getValue().getNumber()),
                () -> assertEquals(SeatType.COUPLE, saved.getValue().getType()));
        verify(seats).existsByHallIdAndRowAndNumber(" h1 ", "A", 2);
    }

    static Stream<CreateCinemaCommand> invalidCinemas() {
        return Stream.concat(Stream.of((CreateCinemaCommand) null),
                Stream.of(null, "", " ").flatMap(value -> Stream.of(
                        new CreateCinemaCommand(value, "Address"), new CreateCinemaCommand("Cinema", value))));
    }

    @ParameterizedTest
    @MethodSource("invalidCinemas")
    void invalidCinemaDoesNotSave(CreateCinemaCommand command) {
        assertThrows(ValidationException.class, () -> createCinema.handle(command));
        verifyNoInteractions(cinemas);
    }

    static Stream<CreateHallCommand> invalidHalls() {
        return Stream.concat(Stream.of((CreateHallCommand) null),
                Stream.of(null, "", " ").flatMap(value -> Stream.of(
                        new CreateHallCommand(value, "Hall"), new CreateHallCommand("c1", value))));
    }

    @ParameterizedTest
    @MethodSource("invalidHalls")
    void invalidHallDoesNotAccessRepositories(CreateHallCommand command) {
        assertThrows(ValidationException.class, () -> createHall.handle(command));
        verifyNoInteractions(cinemas, halls);
    }

    static Stream<CreateSeatCommand> invalidSeats() {
        return Stream.of(null,
                new CreateSeatCommand(null, "A", 1, "NORMAL"),
                new CreateSeatCommand("", "A", 1, "NORMAL"),
                new CreateSeatCommand(" ", "A", 1, "NORMAL"),
                new CreateSeatCommand("h1", null, 1, "NORMAL"),
                new CreateSeatCommand("h1", "", 1, "NORMAL"),
                new CreateSeatCommand("h1", " ", 1, "NORMAL"),
                new CreateSeatCommand("h1", "A", null, "NORMAL"),
                new CreateSeatCommand("h1", "A", 0, "NORMAL"),
                new CreateSeatCommand("h1", "A", -1, "NORMAL"),
                new CreateSeatCommand("h1", "A", 1, null),
                new CreateSeatCommand("h1", "A", 1, ""),
                new CreateSeatCommand("h1", "A", 1, "UNKNOWN"),
                new CreateSeatCommand("h1", "A", 1, "normal"),
                new CreateSeatCommand("h1", "A", 1, " NORMAL "));
    }

    @ParameterizedTest
    @MethodSource("invalidSeats")
    void invalidSeatIsRejectedBeforeParentLookup(CreateSeatCommand command) {
        assertThrows(ValidationException.class, () -> createSeat.handle(command));
        verifyNoInteractions(halls, seats);
    }

    @Test
    void missingCinemaDoesNotSaveHall() {
        assertThrows(NotFoundException.class, () -> createHall.handle(new CreateHallCommand("missing", "Hall")));
        verifyNoInteractions(halls);
    }

    @Test
    void missingHallDoesNotSaveSeat() {
        assertThrows(NotFoundException.class, () -> createSeat.handle(new CreateSeatCommand("missing", "A", 1, "NORMAL")));
        verifyNoInteractions(seats);
    }

    @Test
    void duplicateNormalizedNameDoesNotSave() {
        existingParents();
        when(halls.existsByCinemaIdAndName(" c1 ", "Hall")).thenReturn(true);
        assertThrows(ConflictException.class, () -> createHall.handle(new CreateHallCommand(" c1 ", " Hall ")));
        verify(halls, never()).save(any());
    }

    @Test
    void duplicateNormalizedPositionDoesNotSave() {
        existingParents();
        when(seats.existsByHallIdAndRowAndNumber(" h1 ", "A", 1)).thenReturn(true);
        assertThrows(ConflictException.class, () -> createSeat.handle(new CreateSeatCommand(" h1 ", " a ", 1, "VIP")));
        verify(seats, never()).save(any());
    }

    @Test
    void repositoryConflictsPropagateAfterSuccessfulPrechecks() {
        existingParents();
        var conflict = new ConflictException("concurrent insert");
        doThrow(conflict).when(cinemas).save(any());
        doThrow(conflict).when(halls).save(any());
        doThrow(conflict).when(seats).save(any());
        assertSame(conflict, assertThrows(ConflictException.class,
                () -> createCinema.handle(new CreateCinemaCommand("Cinema", "Address"))));
        assertSame(conflict, assertThrows(ConflictException.class,
                () -> createHall.handle(new CreateHallCommand(" c1 ", "Hall"))));
        assertSame(conflict, assertThrows(ConflictException.class,
                () -> createSeat.handle(new CreateSeatCommand(" h1 ", "A", 1, "NORMAL"))));
    }

    @Test
    void domainValidationBecomesApplicationValidation() {
        existingParents();
        assertThrows(ValidationException.class, () -> new CreateHallCommandHandler(cinemas, halls, () -> "")
                .handle(new CreateHallCommand(" c1 ", "Hall")));
        assertThrows(ValidationException.class, () -> new CreateSeatCommandHandler(halls, seats, () -> "")
                .handle(new CreateSeatCommand(" h1 ", "A", 1, "NORMAL")));
        verify(halls, never()).save(any());
        verify(seats, never()).save(any());
    }

    @Test
    void unexpectedRepositoryFailuresAreNotValidation() {
        existingParents();
        var failure = new IllegalStateException("storage unavailable");
        doThrow(failure).when(cinemas).save(any());
        doThrow(failure).when(halls).save(any());
        doThrow(failure).when(seats).save(any());
        assertSame(failure, assertThrows(IllegalStateException.class,
                () -> createCinema.handle(new CreateCinemaCommand("Cinema", "Address"))));
        assertSame(failure, assertThrows(IllegalStateException.class,
                () -> createHall.handle(new CreateHallCommand(" c1 ", "Hall"))));
        assertSame(failure, assertThrows(IllegalStateException.class,
                () -> createSeat.handle(new CreateSeatCommand(" h1 ", "A", 1, "NORMAL"))));
    }
}

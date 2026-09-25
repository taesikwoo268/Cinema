package cinema.application.screenings.commands;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import cinema.application.common.exceptions.ConflictException;
import cinema.application.common.exceptions.NotFoundException;
import cinema.application.common.exceptions.ValidationException;
import cinema.application.common.interfaces.ScreeningRepository;
import cinema.domain.common.Money;
import cinema.domain.screening.Screening;
import cinema.domain.screening.ScreeningStatus;

class CancelScreeningCommandHandlerTest {

    private static final Instant START = Instant.parse("2026-10-01T10:00:00Z");
    private static final Instant END = Instant.parse("2026-10-01T12:00:00Z");
    private static final Money PRICE = new Money(new BigDecimal("100000.00"), Currency.getInstance("VND"));

    private final ScreeningRepository screenings = mock(ScreeningRepository.class);
    private final CancelScreeningCommandHandler handler = new CancelScreeningCommandHandler(screenings);

    private Screening screening(ScreeningStatus status) {
        return new Screening("screening-1", "movie-1", "hall-1", START, END, PRICE, status);
    }

    @Test
    void cancelsScheduledScreeningAndSavesIt() {
        Screening screening = screening(ScreeningStatus.SCHEDULED);
        when(screenings.findById("screening-1")).thenReturn(Optional.of(screening));

        assertNull(handler.handle(new CancelScreeningCommand("screening-1")));

        assertEquals(ScreeningStatus.CANCELLED, screening.getStatus());
        verify(screenings).save(screening);
    }

    private void assertInvalid(CancelScreeningCommand command) {
        var exception = assertThrows(ValidationException.class, () -> handler.handle(command));
        assertEquals(ValidationException.class, exception.getClass());
        verifyNoInteractions(screenings);
    }

    @Test
    void rejectsNullCommandBeforeRepositoryLookup() {
        assertInvalid(null);
    }

    @Test
    void rejectsNullScreeningIdBeforeRepositoryLookup() {
        assertInvalid(new CancelScreeningCommand(null));
    }

    @Test
    void rejectsBlankScreeningIdBeforeRepositoryLookup() {
        assertInvalid(new CancelScreeningCommand(" \t\n"));
    }

    @Test
    void missingScreeningThrowsNotFoundAndDoesNotSave() {
        when(screenings.findById("missing")).thenReturn(Optional.empty());

        var exception = assertThrows(NotFoundException.class,
                () -> handler.handle(new CancelScreeningCommand("missing")));
        assertEquals(NotFoundException.class, exception.getClass());
        verify(screenings, never()).save(any());
    }

    @Test
    void cancelledScreeningBecomesConflictAndDoesNotSave() {
        Screening screening = screening(ScreeningStatus.CANCELLED);
        when(screenings.findById("screening-1")).thenReturn(Optional.of(screening));

        var exception = assertThrows(ConflictException.class,
                () -> handler.handle(new CancelScreeningCommand("screening-1")));
        assertEquals(ConflictException.class, exception.getClass());
        assertTrue(exception.getMessage() != null && !exception.getMessage().isBlank());
        verify(screenings, never()).save(any());
    }

    @Test
    void completedScreeningBecomesConflictAndDoesNotSave() {
        Screening screening = screening(ScreeningStatus.COMPLETED);
        when(screenings.findById("screening-1")).thenReturn(Optional.of(screening));

        var exception = assertThrows(ConflictException.class,
                () -> handler.handle(new CancelScreeningCommand("screening-1")));
        assertEquals(ConflictException.class, exception.getClass());
        assertTrue(exception.getMessage() != null && !exception.getMessage().isBlank());
        verify(screenings, never()).save(any());
    }
}

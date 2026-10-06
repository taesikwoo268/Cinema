package cinema.application.bookings.commands;

import cinema.application.common.exceptions.ForbiddenException;
import cinema.application.common.exceptions.NotFoundException;
import cinema.application.common.exceptions.ValidationException;
import cinema.application.common.interfaces.*;
import cinema.domain.booking.*;
import cinema.domain.common.DomainException;
import cinema.domain.common.Money;
import cinema.domain.customer.Customer;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CancelBookingCommandHandlerTest {
    private final CurrentUser user = mock(CurrentUser.class);
    private final CustomerRepository customers = mock(CustomerRepository.class);
    private final BookingRepository bookings = mock(BookingRepository.class);
    private final SeatHoldService holds = mock(SeatHoldService.class);
    private final CancelBookingCommandHandler handler = new CancelBookingCommandHandler(user, customers, bookings, holds);
    private final CancelBookingCommand command = new CancelBookingCommand("b1");

    @BeforeEach
    void setup() {
        when(user.isAuthenticated()).thenReturn(true);
        when(user.getUserId()).thenReturn("u1");
        when(customers.findByUserId("u1")).thenReturn(Optional.of(new Customer("c1", "u1", "Customer")));
    }

    private Booking booking(String customerId, BookingStatus status) {
        Money price = new Money(BigDecimal.TEN, Currency.getInstance("USD"));
        Booking booking = spy(new Booking("b1", customerId, "s1",
                List.of(new BookingItem("a1", price), new BookingItem("a2", price)),
                new Money(new BigDecimal("20"), price.currency()), status,
                Instant.parse("2026-09-28T00:00:00Z")));
        when(bookings.findById("b1")).thenReturn(Optional.of(booking));
        return booking;
    }

    @Test
    void nullCommandRejectedBeforeDependencies() {
        assertThrows(ValidationException.class, () -> handler.handle(null));
        verifyNoInteractions(user, customers, bookings, holds);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t\n"})
    void invalidBookingIdRejectedBeforeDependencies(String id) {
        assertThrows(ValidationException.class, () -> handler.handle(new CancelBookingCommand(id)));
        verifyNoInteractions(user, customers, bookings, holds);
    }

    @Test
    void unauthenticatedRejected() {
        when(user.isAuthenticated()).thenReturn(false);
        assertThrows(ForbiddenException.class, () -> handler.handle(command));
        verify(user, never()).getUserId();
        verifyNoInteractions(customers, bookings, holds);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t\n"})
    void invalidAuthenticatedUserIdRejected(String id) {
        when(user.getUserId()).thenReturn(id);
        assertThrows(ForbiddenException.class, () -> handler.handle(command));
        verifyNoInteractions(customers, bookings, holds);
    }

    @Test
    void missingCustomerRejected() {
        when(customers.findByUserId("u1")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> handler.handle(command));
        verifyNoInteractions(bookings, holds);
    }

    @Test
    void missingBookingRejectedWithoutReleaseOrSave() {
        when(bookings.findById("b1")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> handler.handle(command));
        verifyNoInteractions(holds);
        verify(bookings, never()).save(any());
    }

    @Test
    void anotherCustomersBookingRejectedBeforeCancellation() {
        Booking booking = booking("other-customer", BookingStatus.PENDING);
        assertThrows(ForbiddenException.class, () -> handler.handle(command));
        assertEquals(BookingStatus.PENDING, booking.getStatus());
        verify(booking, never()).cancel();
        verifyNoInteractions(holds);
        verify(bookings, never()).save(any());
    }

    @Test
    void cancelsReleasesEveryHoldAndSavesInOrder() {
        Booking booking = booking("c1", BookingStatus.PENDING);
        doAnswer(invocation -> {
            assertEquals(BookingStatus.CANCELLED, booking.getStatus());
            return null;
        }).when(holds).release(anyString(), anyString(), anyString());
        assertNull(handler.handle(command));
        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        var order = inOrder(user, customers, bookings, booking, holds);
        order.verify(user).isAuthenticated();
        order.verify(user).getUserId();
        order.verify(customers).findByUserId("u1");
        order.verify(bookings).findById("b1");
        order.verify(booking).cancel();
        order.verify(holds).release("c1", "s1", "a1");
        order.verify(holds).release("c1", "s1", "a2");
        order.verify(bookings).save(booking);
        verifyNoMoreInteractions(holds);
        verify(customers, never()).save(any());
    }

    @ParameterizedTest
    @EnumSource(value = BookingStatus.class, names = {"CONFIRMED", "CANCELLED"})
    void domainRejectsTerminalStatesWithoutReleaseOrSave(BookingStatus status) {
        Booking booking = booking("c1", status);
        assertThrows(DomainException.class, () -> handler.handle(command));
        verify(booking).cancel();
        assertEquals(status, booking.getStatus());
        verifyNoInteractions(holds);
        verify(bookings, never()).save(any());
    }
}

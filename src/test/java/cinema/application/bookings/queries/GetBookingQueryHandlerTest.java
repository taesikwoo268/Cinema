package cinema.application.bookings.queries;

import cinema.application.common.exceptions.ForbiddenException;
import cinema.application.common.exceptions.NotFoundException;
import cinema.application.common.exceptions.ValidationException;
import cinema.application.common.interfaces.BookingRepository;
import cinema.application.common.interfaces.CurrentUser;
import cinema.application.common.interfaces.CustomerRepository;
import cinema.application.common.models.BookingView;
import cinema.domain.booking.Booking;
import cinema.domain.booking.BookingItem;
import cinema.domain.booking.BookingStatus;
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

class GetBookingQueryHandlerTest {
    private final CurrentUser user = mock(CurrentUser.class);
    private final CustomerRepository customers = mock(CustomerRepository.class);
    private final BookingRepository bookings = mock(BookingRepository.class);
    private final GetBookingQueryHandler handler = new GetBookingQueryHandler(user, customers, bookings);
    private final GetBookingQuery query = new GetBookingQuery("booking-1");
    private final Money total = new Money(new BigDecimal("123.4500"), Currency.getInstance("EUR"));
    private final Instant created = Instant.parse("2026-09-28T00:00:00Z");

    @BeforeEach
    void setup() {
        when(user.isAuthenticated()).thenReturn(true);
        when(user.getUserId()).thenReturn("user-1");
        when(customers.findByUserId("user-1"))
                .thenReturn(Optional.of(new Customer("customer-1", "user-1", "Customer")));
    }

    private Booking booking(String customerId, BookingStatus status) {
        Booking booking = spy(new Booking("booking-1", customerId, "screening-2",
                List.of(new BookingItem("seat-1", total)), total, status, created));
        when(bookings.findById("booking-1")).thenReturn(Optional.of(booking));
        return booking;
    }

    @Test
    void nullQueryRejectedBeforeDependencies() {
        assertThrows(ValidationException.class, () -> handler.handle(null));
        verifyNoInteractions(user, customers, bookings);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t\n"})
    void invalidBookingIdRejectedBeforeDependencies(String id) {
        assertThrows(ValidationException.class, () -> handler.handle(new GetBookingQuery(id)));
        verifyNoInteractions(user, customers, bookings);
    }

    @Test
    void unauthenticatedRejectedBeforeReadingUserId() {
        when(user.isAuthenticated()).thenReturn(false);
        assertThrows(ForbiddenException.class, () -> handler.handle(query));
        verify(user, never()).getUserId();
        verifyNoInteractions(customers, bookings);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t\n"})
    void invalidAuthenticatedUserIdRejected(String id) {
        when(user.getUserId()).thenReturn(id);
        assertThrows(ForbiddenException.class, () -> handler.handle(query));
        verifyNoInteractions(customers, bookings);
    }

    @Test
    void missingCustomerDoesNotLookupBooking() {
        when(customers.findByUserId("user-1")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> handler.handle(query));
        verify(customers).findByUserId("user-1");
        verifyNoMoreInteractions(customers);
        verifyNoInteractions(bookings);
    }

    @Test
    void missingBookingReturnsNotFoundWithoutSaving() {
        when(bookings.findById("booking-1")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> handler.handle(query));
        verify(customers).findByUserId("user-1");
        verify(bookings).findById("booking-1");
        verifyNoMoreInteractions(customers, bookings);
    }

    @Test
    void anotherCustomersBookingIsForbiddenWithoutMappingOrMutation() {
        Booking booking = booking("other-customer", BookingStatus.PENDING);
        assertThrows(ForbiddenException.class, () -> handler.handle(query));
        verify(booking, never()).getTotalAmount();
        verify(booking, never()).confirm();
        verify(booking, never()).cancel();
        verify(bookings, never()).save(any());
        verify(customers, never()).save(any());
        assertEquals(BookingStatus.PENDING, booking.getStatus());
    }

    @ParameterizedTest
    @EnumSource(BookingStatus.class)
    void ownedBookingMapsAllFieldsWithoutMutation(BookingStatus status) {
        Booking booking = booking("customer-1", status);
        var originalItems = booking.getItems();
        BookingView result = handler.handle(query);
        assertEquals(new BookingView("booking-1", "screening-2", status.name(),
                new BigDecimal("123.4500"), "EUR"), result);
        assertEquals(4, result.totalAmount().scale());
        assertEquals(status, booking.getStatus());
        assertEquals(originalItems, booking.getItems());
        assertEquals(total, booking.getTotalAmount());
        assertEquals(created, booking.getCreatedAt());
        verify(booking, never()).confirm();
        verify(booking, never()).cancel();
        verify(booking, never()).calculateTotal();
        var order = inOrder(user, customers, bookings);
        order.verify(user).isAuthenticated();
        order.verify(user).getUserId();
        order.verify(customers).findByUserId("user-1");
        order.verify(bookings).findById("booking-1");
        verifyNoMoreInteractions(customers, bookings);
    }
}

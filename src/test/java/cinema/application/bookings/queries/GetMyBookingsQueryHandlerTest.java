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

class GetMyBookingsQueryHandlerTest {
    private final CurrentUser user = mock(CurrentUser.class);
    private final CustomerRepository customers = mock(CustomerRepository.class);
    private final BookingRepository bookings = mock(BookingRepository.class);
    private final GetMyBookingsQueryHandler handler = new GetMyBookingsQueryHandler(user, customers, bookings);
    private final GetMyBookingsQuery query = new GetMyBookingsQuery();
    private final Instant createdAt = Instant.parse("2026-09-28T00:00:00Z");

    @BeforeEach
    void setup() {
        when(user.isAuthenticated()).thenReturn(true);
        when(user.getUserId()).thenReturn("user-1");
        when(customers.findByUserId("user-1"))
                .thenReturn(Optional.of(new Customer("customer-1", "user-1", "Customer")));
    }

    private Booking booking(String id, String screening, BookingStatus status, String amount, String currency) {
        Money price = new Money(new BigDecimal(amount), Currency.getInstance(currency));
        return spy(new Booking(id, "customer-1", screening, List.of(new BookingItem("seat-1", price)),
                price, status, createdAt));
    }

    private void verifyReadOnly() {
        verify(customers).findByUserId("user-1");
        verify(bookings).findByCustomerId("customer-1");
        verifyNoMoreInteractions(customers, bookings);
    }

    @Test
    void rejectsNullQueryBeforeDependencies() {
        assertThrows(ValidationException.class, () -> handler.handle(null));
        verifyNoInteractions(user, customers, bookings);
    }

    @Test
    void rejectsUnauthenticatedBeforeReadingUserId() {
        when(user.isAuthenticated()).thenReturn(false);
        assertThrows(ForbiddenException.class, () -> handler.handle(query));
        verify(user, never()).getUserId();
        verifyNoInteractions(customers, bookings);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t\n"})
    void rejectsInvalidAuthenticatedUserId(String id) {
        when(user.getUserId()).thenReturn(id);
        assertThrows(ForbiddenException.class, () -> handler.handle(query));
        verifyNoInteractions(customers, bookings);
    }

    @Test
    void missingCustomerDoesNotQueryBookings() {
        when(customers.findByUserId("user-1")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> handler.handle(query));
        verify(customers).findByUserId("user-1");
        verifyNoMoreInteractions(customers);
        verifyNoInteractions(bookings);
    }

    @Test
    void noBookingsReturnsEmptyList() {
        when(bookings.findByCustomerId("customer-1")).thenReturn(List.of());
        assertEquals(List.of(), handler.handle(query));
        verifyReadOnly();
    }

    @ParameterizedTest
    @EnumSource(BookingStatus.class)
    void mapsEveryFieldAndEnumNameWithoutMutation(BookingStatus status) {
        Booking booking = booking("booking-1", "screening-2", status, "12.50", "USD");
        var originalItems = booking.getItems();
        when(bookings.findByCustomerId("customer-1")).thenReturn(List.of(booking));
        assertEquals(List.of(new BookingView("booking-1", "screening-2", status.name(),
                new BigDecimal("12.50"), "USD")), handler.handle(query));
        assertEquals(status, booking.getStatus());
        assertEquals(originalItems, booking.getItems());
        assertEquals(createdAt, booking.getCreatedAt());
        verify(booking, never()).confirm();
        verify(booking, never()).cancel();
        verify(booking, never()).calculateTotal();
        verifyReadOnly();
    }

    @Test
    void preservesRepositoryOrderAndCurrencyForMultipleBookings() {
        Booking first = booking("z-booking", "s2", BookingStatus.CANCELLED, "25000", "VND");
        Booking second = booking("a-booking", "s1", BookingStatus.PENDING, "0.125", "EUR");
        when(bookings.findByCustomerId("customer-1")).thenReturn(List.of(first, second));
        assertEquals(List.of(
                new BookingView("z-booking", "s2", "CANCELLED", new BigDecimal("25000"), "VND"),
                new BookingView("a-booking", "s1", "PENDING", new BigDecimal("0.125"), "EUR")), handler.handle(query));
        var order = inOrder(user, customers, bookings);
        order.verify(user).isAuthenticated();
        order.verify(user).getUserId();
        order.verify(customers).findByUserId("user-1");
        order.verify(bookings).findByCustomerId("customer-1");
        for (Booking booking : List.of(first, second)) {
            verify(booking, never()).confirm();
            verify(booking, never()).cancel();
            verify(booking, never()).calculateTotal();
        }
        verifyReadOnly();
    }
}

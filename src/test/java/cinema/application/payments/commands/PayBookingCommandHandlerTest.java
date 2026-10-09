package cinema.application.payments.commands;

import cinema.application.common.exceptions.ConflictException;
import cinema.application.common.exceptions.ForbiddenException;
import cinema.application.common.exceptions.NotFoundException;
import cinema.application.common.exceptions.ValidationException;
import cinema.application.common.interfaces.*;
import cinema.application.common.models.PaymentResult;
import cinema.domain.booking.Booking;
import cinema.domain.booking.BookingItem;
import cinema.domain.booking.BookingStatus;
import cinema.domain.common.Money;
import cinema.domain.customer.Customer;
import cinema.domain.payment.Payment;
import cinema.domain.payment.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PayBookingCommandHandlerTest {

    private final CurrentUser currentUser = mock(CurrentUser.class);
    private final CustomerRepository customers = mock(CustomerRepository.class);
    private final BookingRepository bookings = mock(BookingRepository.class);
    private final PaymentRepository payments = mock(PaymentRepository.class);
    private final PaymentGateway paymentGateway = mock(PaymentGateway.class);
    private int idGenerated;
    private final Supplier<String> idGenerator = () -> {idGenerated++ ;return  "payment-123";};

    private final PayBookingCommandHandler handler = new PayBookingCommandHandler(currentUser
            , customers, bookings, payments, paymentGateway, idGenerator, mock(TicketRepository.class), mock(SeatReservationRepository.class));

    private Booking booking(String customerId, BookingStatus status) {
        Money money = new Money(BigDecimal.valueOf(12345), Currency.getInstance("VND"));
        Booking booking = spy(new Booking("booking-123", customerId
                , "screening-1", List.of(new BookingItem("seat-1", money))
                , money, status, Instant.parse("2026-10-02T00:00:00Z")));
        when(bookings.findById("booking-123")).thenReturn(Optional.of(booking));
        return booking;
    }


    @Test
    void nullCommandRejectedBeforeDependencies() {
        assertThrows(ValidationException.class, () -> handler.handle(null));
        verifyNoInteractions(currentUser, customers, bookings, paymentGateway, payments);
        assertEquals(0, idGenerated);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" \t\n"})
    void validatedBookingIdRejectedBeforeDependencies(String bookingId) {
        assertThrows(ValidationException.class, () -> handler.handle(new PayBookingCommand(bookingId)));

        verifyNoInteractions(currentUser, customers, bookings, paymentGateway, payments);
        assertEquals(0, idGenerated);
    }

    @Test
    void unauthenticatedUserRejectedDoesNotGetUserId() {
        when(currentUser.isAuthenticated()).thenReturn(false);

        assertThrows(ForbiddenException.class, () -> handler.handle(new PayBookingCommand("booking-123")));

        verify(currentUser).isAuthenticated();
        verify(currentUser, never()).getUserId();
        verifyNoInteractions(customers, bookings, paymentGateway, payments);
        assertEquals(0, idGenerated);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" \t\n"})
    void invalidAuthenticatedUserIdRejected(String userId) {
        when(currentUser.isAuthenticated()).thenReturn(true);
        when(currentUser.getUserId()).thenReturn(userId);

        assertThrows(ForbiddenException.class, () -> handler.handle(new PayBookingCommand("booking-123")));

        verify(currentUser).getUserId();
        verifyNoInteractions(customers, bookings, paymentGateway, payments);
        assertEquals(0, idGenerated);
    }

    @Test
    void customerMissingRejected() {
        when(currentUser.isAuthenticated()).thenReturn(true);
        when(currentUser.getUserId()).thenReturn("user-123");
        when(customers.findByUserId("user-123")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> handler.handle(new PayBookingCommand("booking-123")));

        verify(customers).findByUserId("user-123");
        verifyNoInteractions(bookings, paymentGateway, payments);
        assertEquals(0, idGenerated);
    }

    @Test
    void bookingMissingRejectedWithoutSave() {
        Customer customer = new Customer("customer-123", "user-123", "userxyz");

        when(currentUser.isAuthenticated()).thenReturn(true);
        when(currentUser.getUserId()).thenReturn("user-123");
        when(customers.findByUserId("user-123")).thenReturn(Optional.of(customer));
        when(bookings.findById("booking-123")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> handler.handle(new PayBookingCommand("booking-123")));

        verify(bookings).findById("booking-123");
        verify(bookings, never()).save(any());
        verifyNoInteractions(paymentGateway, payments);
        assertEquals(0, idGenerated);
    }

    @Test
    void bookingBelongsToAnotherCustomerRejectedBeforePendingChecking() {
        Customer customer1 = spy(new Customer("customer-1", "user-1", "userxyz"));

        Booking booking = booking("customer-2", BookingStatus.PENDING);

        when(currentUser.isAuthenticated()).thenReturn(true);
        when(currentUser.getUserId()).thenReturn("user-1");
        when(customers.findByUserId("user-1")).thenReturn(Optional.of(customer1));

        assertThrows(ForbiddenException.class, () -> handler.handle(new PayBookingCommand("booking-123")));

        verify(booking).getCustomerId();
        verify(customer1).getId();
        assertEquals("customer-1", customer1.getId());
        assertEquals("customer-2", booking.getCustomerId());
        verify(booking, never()).getStatus();
        verifyNoInteractions(paymentGateway, payments);
        assertEquals(0, idGenerated);
        verify(bookings, never()).save(any());
    }

    @ParameterizedTest
    @EnumSource(value = BookingStatus.class, names = {"CANCELLED", "CONFIRMED"})
    void notPendingBookingDoesNotCallPaymentGateway(BookingStatus status) {
        Customer customer1 = new Customer("customer-1", "user-1", "userxyz");

        Booking booking = booking("customer-1", status);

        when(currentUser.isAuthenticated()).thenReturn(true);
        when(currentUser.getUserId()).thenReturn("user-1");
        when(customers.findByUserId("user-1")).thenReturn(Optional.of(customer1));

        assertThrows(ConflictException.class, () -> handler.handle(new PayBookingCommand("booking-123")));

        verifyNoInteractions(paymentGateway);
        assertEquals(0, idGenerated);
        verify(booking, never()).confirm();
        verify(bookings, never()).save(any());
    }

    @Test
    void successCallsGatewayExactlyAsExpected() {
        Money money = new Money(BigDecimal.valueOf(12345), Currency.getInstance("VND"));

        Customer customer1 = new Customer("customer-1", "user-1", "userxyz");

        Booking booking = booking("customer-1", BookingStatus.PENDING);

        when(currentUser.isAuthenticated()).thenReturn(true);
        when(currentUser.getUserId()).thenReturn("user-1");
        when(customers.findByUserId("user-1")).thenReturn(Optional.of(customer1));

        PaymentResult result = new PaymentResult(true, "Provider-1");

        when(paymentGateway.pay("booking-123", money)).thenReturn(result);

        handler.handle(new PayBookingCommand("booking-123"));

        verify(paymentGateway).pay("booking-123", money);
    }

    @Test
    void successCreatesAndSavesPaymentCorrectly() {
        Customer customer1 = new Customer("customer-1", "user-1", "userxyz");

        Money money = new Money(BigDecimal.valueOf(12345), Currency.getInstance("VND"));

        Booking booking = booking("customer-1", BookingStatus.PENDING);

        when(currentUser.isAuthenticated()).thenReturn(true);
        when(currentUser.getUserId()).thenReturn("user-1");
        when(customers.findByUserId("user-1")).thenReturn(Optional.of(customer1));


        PaymentResult result = new PaymentResult(true, "Provider-1");

        when(paymentGateway.pay("booking-123", money)).thenReturn(result);

        handler.handle(new PayBookingCommand("booking-123"));

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(payments).save(captor.capture());

        Payment savedPayment = captor.getValue();
        assertEquals(PaymentStatus.SUCCESS, savedPayment.getStatus());
        assertEquals(money, savedPayment.getAmount());
        assertEquals("Provider-1" ,savedPayment.getProviderReference());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" \t\n"})
    void successfulResultRejectByNullOrBlankProviderReference(String provider) {
        Customer customer1 = new Customer("customer-1", "user-1", "userxyz");

        Money money = new Money(BigDecimal.valueOf(12345), Currency.getInstance("VND"));

        Booking booking = booking("customer-1", BookingStatus.PENDING);

        when(currentUser.isAuthenticated()).thenReturn(true);
        when(currentUser.getUserId()).thenReturn("user-1");
        when(customers.findByUserId("user-1")).thenReturn(Optional.of(customer1));

        when(paymentGateway.pay("booking-123", money)).thenReturn(new PaymentResult(true, provider));

        assertThrows(IllegalStateException.class ,
                () -> handler.handle(new PayBookingCommand("booking-123")));

        verify(payments, never()).save(any());
        verify(booking, never()).confirm();
        verify(bookings, never()).save(any());
    }

    @Test
    void successCallsBookingConfirmAndSaveBookingCorrectly() {
        Customer customer1 = new Customer("customer-1", "user-1", "userxyz");

        Money money = new Money(BigDecimal.valueOf(12345), Currency.getInstance("VND"));

        Booking booking = booking("customer-1", BookingStatus.PENDING);

        when(currentUser.isAuthenticated()).thenReturn(true);
        when(currentUser.getUserId()).thenReturn("user-1");
        when(customers.findByUserId("user-1")).thenReturn(Optional.of(customer1));

        PaymentResult result = new PaymentResult(true, "Provider-1");

        when(paymentGateway.pay("booking-123", money)).thenReturn(result);

        handler.handle(new PayBookingCommand("booking-123"));

        verify(booking).confirm();
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        verify(bookings).save(booking);
    }

    @Test
    void successReturnsPaymentResult() {
        Customer customer1 = new Customer("customer-1", "user-1", "userxyz");

        Money money = new Money(BigDecimal.valueOf(12345), Currency.getInstance("VND"));

        Booking booking = booking("customer-1", BookingStatus.PENDING);

        when(currentUser.isAuthenticated()).thenReturn(true);
        when(currentUser.getUserId()).thenReturn("user-1");
        when(customers.findByUserId("user-1")).thenReturn(Optional.of(customer1));

        PaymentResult result = new PaymentResult(true, "Provider-1");

        when(paymentGateway.pay("booking-123", money)).thenReturn(result);

        assertEquals(result, handler.handle(new PayBookingCommand("booking-123")));

        verify(bookings).save(booking);
    }

    @Test
    void failedCreatesAndSavesPaymentCorrectly() {
        Customer customer1 = new Customer("customer-1", "user-1", "userxyz");

        Money money = new Money(BigDecimal.valueOf(12345), Currency.getInstance("VND"));

        Booking booking = booking("customer-1", BookingStatus.PENDING);

        when(currentUser.isAuthenticated()).thenReturn(true);
        when(currentUser.getUserId()).thenReturn("user-1");
        when(customers.findByUserId("user-1")).thenReturn(Optional.of(customer1));

        PaymentResult result = new PaymentResult(false, "Provider-1");

        when(paymentGateway.pay("booking-123", money)).thenReturn(result);

        handler.handle(new PayBookingCommand("booking-123"));

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(payments).save(captor.capture());

        Payment savedPayment = captor.getValue();
        assertEquals(PaymentStatus.FAILED, savedPayment.getStatus());
        assertEquals(booking.getTotalAmount(), savedPayment.getAmount());
        assertEquals("Provider-1" ,savedPayment.getProviderReference());
    }

    @Test
    void failedGateWayDoesNotConfirmOrSaveBooking() {
        Customer customer1 = new Customer("customer-1", "user-1", "userxyz");

        Money money = new Money(BigDecimal.valueOf(12345), Currency.getInstance("VND"));

        Booking booking = booking("customer-1", BookingStatus.PENDING);

        when(currentUser.isAuthenticated()).thenReturn(true);
        when(currentUser.getUserId()).thenReturn("user-1");
        when(customers.findByUserId("user-1")).thenReturn(Optional.of(customer1));

        PaymentResult result = new PaymentResult(false, "Provider-1");

        when(paymentGateway.pay("booking-123", money)).thenReturn(result);

        handler.handle(new PayBookingCommand("booking-123"));

        verify(booking, never()).confirm();
        verify(bookings, never()).save(any());
    }

    @Test
    void failedGateWayReturnsPaymentResultUnsuccessful() {
        Customer customer1 = new Customer("customer-1", "user-1", "userxyz");

        Money money = new Money(BigDecimal.valueOf(12345), Currency.getInstance("VND"));

        Booking booking = booking("customer-1", BookingStatus.PENDING);

        when(currentUser.isAuthenticated()).thenReturn(true);
        when(currentUser.getUserId()).thenReturn("user-1");
        when(customers.findByUserId("user-1")).thenReturn(Optional.of(customer1));

        PaymentResult result = new PaymentResult(false, "Provider-1");

        when(paymentGateway.pay("booking-123", money)).thenReturn(result);

        assertEquals(result, handler.handle(new PayBookingCommand("booking-123")));
    }

    @Test
    void verifyImportantInteractionOrder() {
        Customer customer1 = new Customer("customer-1", "user-1", "userxyz");

        Money money = new Money(BigDecimal.valueOf(12345), Currency.getInstance("VND"));

        Booking booking = booking("customer-1", BookingStatus.PENDING);

        when(currentUser.isAuthenticated()).thenReturn(true);
        when(currentUser.getUserId()).thenReturn("user-1");
        when(customers.findByUserId("user-1")).thenReturn(Optional.of(customer1));

        PaymentResult result = new PaymentResult(true, "Provider-1");

        when(paymentGateway.pay("booking-123", money)).thenReturn(result);

        handler.handle(new PayBookingCommand("booking-123"));

        InOrder inOrder = inOrder(paymentGateway, payments, booking, bookings);

        inOrder.verify(paymentGateway).pay("booking-123", money);
        inOrder.verify(payments).save(any(Payment.class));
        inOrder.verify(booking).confirm();
        inOrder.verify(bookings).save(booking);
    }
}

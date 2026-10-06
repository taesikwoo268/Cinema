package cinema.application.payments.commands;

import cinema.application.common.exceptions.*;
import cinema.application.common.interfaces.*;
import cinema.application.common.models.*;
import cinema.domain.booking.Booking;
import cinema.domain.booking.BookingStatus;
import cinema.domain.customer.Customer;
import cinema.domain.payment.Payment;
import cinema.domain.payment.PaymentStatus;

import java.util.function.Supplier;

public final class PayBookingCommandHandler implements UseCase<PayBookingCommand, PaymentResult> {

    CurrentUser currentUser;
    CustomerRepository customers;
    BookingRepository bookings;
    PaymentRepository payments;
    PaymentGateway paymentGateway;
    Supplier<String> idGenerator;

    public PayBookingCommandHandler(CurrentUser currentUser, CustomerRepository customers
            , BookingRepository bookings, PaymentRepository payments
            , PaymentGateway paymentGateway, Supplier<String> idGenerator) {
        this.currentUser = currentUser;
        this.customers = customers;
        this.bookings = bookings;
        this.payments = payments;
        this.paymentGateway = paymentGateway;
        this.idGenerator = idGenerator;
    }

    @Override public PaymentResult handle(PayBookingCommand input) {

        if (input == null) {
            throw new ValidationException("PayBookingCommand must not be null");
        }

        String bookingId = input.bookingId();
        if (bookingId == null || bookingId.isBlank()) {
            throw new ValidationException("BookingId must not be blank");
        }


        if (!this.currentUser.isAuthenticated()) {
            throw new ForbiddenException("User must be Authenticate");
        }

        String userId = this.currentUser.getUserId();
        if (userId == null || userId.isBlank()) {
            throw new ForbiddenException("userId must not be blank");
        }

        Customer customer = customers.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("customer not found"));

        Booking booking = bookings.findById(input.bookingId())
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        if (!customer.getId().equals(booking.getCustomerId())) {
                throw new ForbiddenException("Booking does not belong to customer");
        }

        if (booking.getStatus() != BookingStatus.PENDING) {
                throw new ConflictException("Booking is not pending");
        }

        PaymentResult paymentResult = paymentGateway.pay(bookingId, booking.getTotalAmount());
        if (!paymentResult.successful()) {
            Payment failPayment = new Payment(idGenerator.get(), bookingId, booking.getTotalAmount()
                    , PaymentStatus.FAILED, paymentResult.providerReference());

            payments.save(failPayment);
            return paymentResult;
        }

        if (paymentResult.providerReference() == null || paymentResult.providerReference().isBlank()) {
            throw new IllegalStateException("providerReference must not be null");
        }

        Payment successPayment = new Payment(idGenerator.get(), bookingId, booking.getTotalAmount()
                , PaymentStatus.SUCCESS, paymentResult.providerReference());

        payments.save(successPayment);

        booking.confirm();

        bookings.save(booking);

        return paymentResult;
    }
}

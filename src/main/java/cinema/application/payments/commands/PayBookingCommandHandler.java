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

    private final CurrentUser currentUser;
    private final CustomerRepository customers;
    private final BookingRepository bookings;
    private final PaymentRepository payments;
    private final PaymentGateway paymentGateway;
    private final Supplier<String> idGenerator;
    private final TicketRepository tickets;
    private final SeatReservationRepository reservations;

    public PayBookingCommandHandler(CurrentUser currentUser, CustomerRepository customers
            , BookingRepository bookings, PaymentRepository payments
            , PaymentGateway paymentGateway, Supplier<String> idGenerator,
            TicketRepository tickets, SeatReservationRepository reservations) {
        this.currentUser = currentUser;
        this.customers = customers;
        this.bookings = bookings;
        this.payments = payments;
        this.paymentGateway = paymentGateway;
        this.idGenerator = idGenerator;
        this.tickets = tickets;
        this.reservations = reservations;
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

        synchronized (bookings) {
            Booking booking = bookings.findById(input.bookingId())
                    .orElseThrow(() -> new NotFoundException("Booking not found"));

            if (!customer.getId().equals(booking.getCustomerId())) {
                    throw new ForbiddenException("Booking does not belong to customer");
            }

            if (booking.getStatus() == BookingStatus.CANCELLED) throw new ConflictException("Booking is cancelled");
            var successful = payments.findByBookingId(bookingId).stream()
                    .filter(p -> p.getStatus() == PaymentStatus.SUCCESS).findFirst();
            if (successful.isPresent()) {
                finalizeBooking(booking);
                return new PaymentResult(true, successful.get().getProviderReference());
            }
            if (booking.getStatus() != BookingStatus.PENDING) {
                    throw new ConflictException("Booking is not pending");
            }

            for (var item : booking.getItems()) {
                if (reservations.isBooked(booking.getScreeningId(), item.seatId())) {
                    throw new ConflictException("Seat already booked for screening");
                }
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

            finalizeBooking(booking);

            return paymentResult;
        }
    }

    private void finalizeBooking(Booking booking) {
        if (booking.getStatus() == BookingStatus.PENDING) booking.confirm();
        bookings.save(booking);
        // Reservations precede tickets and remain permanent, including during retry recovery.
        for (var item : booking.getItems()) {
            reservations.markBooked(booking.getScreeningId(), item.seatId(), booking.getId());
        }
        for (var item : booking.getItems()) {
            String key = booking.getId().length() + ":" + booking.getId() + item.seatId();
            String id = java.util.UUID.nameUUIDFromBytes(key.getBytes(java.nio.charset.StandardCharsets.UTF_8)).toString();
            if (tickets.findById(id).isEmpty()) {
                tickets.save(new cinema.domain.ticket.Ticket(id, booking.getId(), booking.getScreeningId(),
                        item.seatId(), "TICKET-" + id, cinema.domain.ticket.TicketStatus.VALID));
            }
        }
    }
}

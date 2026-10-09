package cinema.application.bookings.commands;

import cinema.application.common.exceptions.ForbiddenException;
import cinema.application.common.exceptions.NotFoundException;
import cinema.application.common.exceptions.ValidationException;
import cinema.application.common.interfaces.BookingRepository;
import cinema.application.common.interfaces.CurrentUser;
import cinema.application.common.interfaces.CustomerRepository;
import cinema.application.common.interfaces.SeatHoldService;
import cinema.application.common.interfaces.UseCase;

public final class CancelBookingCommandHandler implements UseCase<CancelBookingCommand, Void> {
    private final CurrentUser currentUser;
    private final CustomerRepository customers;
    private final BookingRepository bookings;
    private final SeatHoldService holds;

    public CancelBookingCommandHandler(CurrentUser currentUser, CustomerRepository customers,
            BookingRepository bookings, SeatHoldService holds) {
        this.currentUser = currentUser;
        this.customers = customers;
        this.bookings = bookings;
        this.holds = holds;
    }

    @Override
    public Void handle(CancelBookingCommand input) {
        if (input == null) {
            throw new ValidationException("CancelBookingCommand must not be null");
        }
        String bookingId = input.bookingId();
        if (bookingId == null || bookingId.isBlank()) {
            throw new ValidationException("Booking ID must not be blank");
        }
        if (!currentUser.isAuthenticated()) {
            throw new ForbiddenException("Authentication required");
        }
        String userId = currentUser.getUserId();
        if (userId == null || userId.isBlank()) {
            throw new ForbiddenException("Authenticated user ID must not be blank");
        }
        var customer = customers.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Customer not found for user: " + userId));
        synchronized (bookings) {
            var booking = bookings.findById(bookingId)
                    .orElseThrow(() -> new NotFoundException("Booking not found: " + bookingId));
            if (!customer.getId().equals(booking.getCustomerId())) {
                throw new ForbiddenException("Booking belongs to another customer");
            }
            try {
                booking.cancel();
            } catch (cinema.domain.common.DomainException ex) {
                throw new cinema.application.common.exceptions.ConflictException(ex.getMessage());
            }
            for (var item : booking.getItems()) {
                holds.release(booking.getCustomerId(), booking.getScreeningId(), item.seatId());
            }
            bookings.save(booking);
            return null;
        }
    }
}

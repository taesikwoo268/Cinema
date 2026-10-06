package cinema.application.bookings.queries;

import cinema.application.common.exceptions.ForbiddenException;
import cinema.application.common.exceptions.NotFoundException;
import cinema.application.common.exceptions.ValidationException;
import cinema.application.common.interfaces.BookingRepository;
import cinema.application.common.interfaces.CurrentUser;
import cinema.application.common.interfaces.CustomerRepository;
import cinema.application.common.interfaces.UseCase;
import cinema.application.common.models.BookingView;

public final class GetBookingQueryHandler implements UseCase<GetBookingQuery, BookingView> {
    private final CurrentUser currentUser;
    private final CustomerRepository customers;
    private final BookingRepository bookings;

    public GetBookingQueryHandler(CurrentUser currentUser, CustomerRepository customers,
            BookingRepository bookings) {
        this.currentUser = currentUser;
        this.customers = customers;
        this.bookings = bookings;
    }

    @Override
    public BookingView handle(GetBookingQuery input) {
        if (input == null) {
            throw new ValidationException("GetBookingQuery must not be null");
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
        var booking = bookings.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found: " + bookingId));
        if (!customer.getId().equals(booking.getCustomerId())) {
            throw new ForbiddenException("Booking belongs to another customer");
        }
        return new BookingView(booking.getId(), booking.getScreeningId(),
                booking.getStatus().name(), booking.getTotalAmount().amount(),
                booking.getTotalAmount().currency().getCurrencyCode());
    }
}

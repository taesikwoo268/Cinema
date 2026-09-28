package cinema.application.bookings.queries;

import cinema.application.common.exceptions.ForbiddenException;
import cinema.application.common.exceptions.NotFoundException;
import cinema.application.common.exceptions.ValidationException;
import cinema.application.common.interfaces.BookingRepository;
import cinema.application.common.interfaces.CurrentUser;
import cinema.application.common.interfaces.CustomerRepository;
import cinema.application.common.interfaces.UseCase;
import cinema.application.common.models.BookingView;
import java.util.List;

public final class GetMyBookingsQueryHandler implements UseCase<GetMyBookingsQuery, List<BookingView>> {
    private final CurrentUser currentUser;
    private final CustomerRepository customers;
    private final BookingRepository bookings;

    public GetMyBookingsQueryHandler(CurrentUser currentUser, CustomerRepository customers,
            BookingRepository bookings) {
        this.currentUser = currentUser;
        this.customers = customers;
        this.bookings = bookings;
    }

    @Override
    public List<BookingView> handle(GetMyBookingsQuery input) {
        if (input == null) {
            throw new ValidationException("GetMyBookingsQuery must not be null");
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
        return bookings.findByCustomerId(customer.getId()).stream()
                .map(booking -> new BookingView(booking.getId(), booking.getScreeningId(),
                        booking.getStatus().name(), booking.getTotalAmount().amount(),
                        booking.getTotalAmount().currency().getCurrencyCode()))
                .toList();
    }
}

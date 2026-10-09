package cinema.application.tickets.queries;

import cinema.application.common.exceptions.*;
import cinema.application.common.interfaces.*;
import cinema.application.common.models.TicketView;
import cinema.domain.ticket.Ticket;

final class TicketAccess {
    private TicketAccess() { }
    static String customerId(CurrentUser user, CustomerRepository customers) {
        if (!user.isAuthenticated()) throw new ForbiddenException("Authentication required");
        String id = user.getUserId();
        if (id == null || id.isBlank()) throw new ForbiddenException("Authenticated user ID must not be blank");
        return customers.findByUserId(id).orElseThrow(() -> new NotFoundException("Customer not found")).getId();
    }
    static boolean owns(Ticket ticket, String customerId, BookingRepository bookings) {
        return bookings.findById(ticket.getBookingId())
                .map(b -> customerId.equals(b.getCustomerId())).orElse(false);
    }
    static TicketView view(Ticket t) {
        return new TicketView(t.getId(), t.getBookingId(), t.getScreeningId(), t.getSeatId(), t.getTicketCode(), t.getStatus().name());
    }
}

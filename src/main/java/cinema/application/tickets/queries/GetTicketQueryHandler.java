package cinema.application.tickets.queries;

import cinema.application.common.interfaces.*;
import cinema.application.common.exceptions.*;
import cinema.application.common.models.TicketView;
import java.util.List;

public final class GetTicketQueryHandler implements UseCase<GetTicketQuery, TicketView> {
    private final CurrentUser currentUser;
    private final CustomerRepository customers;
    private final BookingRepository bookings;
    private final TicketRepository tickets;
    public GetTicketQueryHandler(CurrentUser currentUser, CustomerRepository customers, BookingRepository bookings, TicketRepository tickets) {
        this.currentUser = currentUser;
        this.customers = customers;
        this.bookings = bookings;
        this.tickets = tickets;
    }
    @Override public TicketView handle(GetTicketQuery input) {
        if (input == null) throw new ValidationException("Query must not be null");
        if (input.ticketId() == null || input.ticketId().isBlank()) throw new ValidationException("Ticket ID must not be blank");
        String customerId = TicketAccess.customerId(currentUser, customers);
        var ticket = tickets.findById(input.ticketId()).orElseThrow(() -> new NotFoundException("Ticket not found"));
        if (!TicketAccess.owns(ticket, customerId, bookings)) throw new ForbiddenException("Ticket belongs to another customer");
        return TicketAccess.view(ticket);
    }
}

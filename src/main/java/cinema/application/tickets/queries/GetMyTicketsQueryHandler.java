package cinema.application.tickets.queries;

import cinema.application.common.interfaces.*;
import cinema.application.common.exceptions.*;
import cinema.application.common.models.TicketView;
import java.util.List;

public final class GetMyTicketsQueryHandler implements UseCase<GetMyTicketsQuery, List<TicketView>> {
    private final CurrentUser currentUser;
    private final CustomerRepository customers;
    private final BookingRepository bookings;
    private final TicketRepository tickets;
    public GetMyTicketsQueryHandler(CurrentUser currentUser, CustomerRepository customers, BookingRepository bookings, TicketRepository tickets) {
        this.currentUser = currentUser;
        this.customers = customers;
        this.bookings = bookings;
        this.tickets = tickets;
    }
    @Override public List<TicketView> handle(GetMyTicketsQuery input) {
        if (input == null) throw new ValidationException("Query must not be null");
        String customerId = TicketAccess.customerId(currentUser, customers);
        return tickets.findByCustomerId(customerId).stream()
                .filter(t -> TicketAccess.owns(t, customerId, bookings)).map(TicketAccess::view).toList();
    }
}

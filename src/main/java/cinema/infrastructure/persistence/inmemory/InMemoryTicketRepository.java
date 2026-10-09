package cinema.infrastructure.persistence.inmemory;

import cinema.application.common.exceptions.ConflictException;
import cinema.application.common.interfaces.BookingRepository;
import cinema.application.common.interfaces.TicketRepository;
import cinema.domain.ticket.Ticket;
import java.util.*;

/** One ticket per booking seat; repeated identical saves retain the original state. */
public final class InMemoryTicketRepository implements TicketRepository {
    private final BookingRepository bookings;
    private final Map<String, Ticket> tickets = new LinkedHashMap<>();
    public InMemoryTicketRepository(BookingRepository bookings) { this.bookings = Objects.requireNonNull(bookings); }
    @Override public synchronized Optional<Ticket> findById(String id) { return Optional.ofNullable(tickets.get(id)); }
    @Override public List<Ticket> findByCustomerId(String customerId) {
        var ids = bookings.findByCustomerId(customerId).stream().map(b -> b.getId()).toList();
        synchronized (this) {
            return tickets.values().stream().filter(t -> ids.contains(t.getBookingId())).toList();
        }
    }
    @Override public synchronized void save(Ticket ticket) {
        Objects.requireNonNull(ticket, "Ticket must not be null");
        for (var existing : tickets.values()) {
            boolean sameSeat = existing.getBookingId().equals(ticket.getBookingId()) && existing.getSeatId().equals(ticket.getSeatId());
            if (existing.getId().equals(ticket.getId()) || sameSeat || existing.getTicketCode().equals(ticket.getTicketCode())) {
                if (sameSeat && existing.getId().equals(ticket.getId()) && existing.getScreeningId().equals(ticket.getScreeningId())
                        && existing.getTicketCode().equals(ticket.getTicketCode())) return;
                throw new ConflictException("Ticket ID, code or booking seat already exists");
            }
        }
        tickets.put(ticket.getId(), ticket);
    }
}

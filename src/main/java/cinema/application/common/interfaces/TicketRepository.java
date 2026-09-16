package cinema.application.common.interfaces;

import java.util.List;
import java.util.Optional;
import java.time.Instant;
import cinema.domain.ticket.Ticket;
public interface TicketRepository {
    Optional<Ticket> findById(String id);
    List<Ticket> findByCustomerId(String customerId);
    void save(Ticket ticket);
}

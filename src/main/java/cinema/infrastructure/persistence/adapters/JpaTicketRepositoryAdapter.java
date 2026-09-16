package cinema.infrastructure.persistence.adapters;

import java.util.List;
import java.util.Optional;
import java.time.Instant;
import cinema.domain.ticket.Ticket;
import cinema.application.common.interfaces.TicketRepository;
/** Unwired until persistence mapping and transactional contracts are implemented. */
public final class JpaTicketRepositoryAdapter implements TicketRepository {
    @Override public Optional<Ticket> findById(String id) { throw new UnsupportedOperationException("Architecture Contract v0.1: not implemented" ); }
    @Override public List<Ticket> findByCustomerId(String customerId) { throw new UnsupportedOperationException("Architecture Contract v0.1: not implemented" ); }
    @Override public void save(Ticket ticket) { throw new UnsupportedOperationException("Architecture Contract v0.1: not implemented" ); }
}

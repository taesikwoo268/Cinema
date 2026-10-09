package cinema.infrastructure.persistence.adapters;

import cinema.application.common.interfaces.TicketRepository;
import cinema.application.common.exceptions.ConflictException;
import cinema.domain.ticket.Ticket;
import cinema.infrastructure.persistence.entities.*;
import cinema.infrastructure.persistence.repositories.*;
import cinema.infrastructure.persistence.mappers.TicketPersistenceMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Repository
@Profile("mysql")
@Transactional
public class JpaTicketRepositoryAdapter implements TicketRepository {
    private final SpringDataTicketRepository data;
    private final TicketPersistenceMapper mapper = new TicketPersistenceMapper();
    @PersistenceContext private EntityManager em;
    public JpaTicketRepositoryAdapter(SpringDataTicketRepository data) {
        this.data = data;
    }
    @Override public Optional<Ticket> findById(String id) {
        return data.lockById(id).map(mapper::toDomain);
    }
    @Override public List<Ticket> findByCustomerId(String customerId) {
        return data.findByCustomerId(customerId).stream().map(mapper::toDomain).toList();
    }
    @Override public void save(Ticket model) {
        Objects.requireNonNull(model);
        var existing = data.lockById(model.getId());
        if (existing.isPresent()) {
            var e = existing.get();
            if (!e.bookingId.equals(model.getBookingId()) || !e.seatId.equals(model.getSeatId())
                    || !e.screeningId.equals(model.getScreeningId()) || !e.ticketCode.equals(model.getTicketCode())) {
                throw new ConflictException("Ticket identity already exists");
            }
            // Re-issuing a valid ticket must never reset USED/CANCELLED state.
            if (e.status == cinema.domain.ticket.TicketStatus.VALID) e.status = model.getStatus();
        } else {
            em.persist(mapper.toEntity(model));
        }
        em.flush();
    }
}

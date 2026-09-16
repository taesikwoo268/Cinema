package cinema.infrastructure.persistence.mappers;

import cinema.domain.ticket.Ticket;
import cinema.infrastructure.persistence.entities.TicketJpaEntity;
public interface TicketPersistenceMapper {
    Ticket toDomain(TicketJpaEntity entity);
    TicketJpaEntity toEntity(Ticket model);
}

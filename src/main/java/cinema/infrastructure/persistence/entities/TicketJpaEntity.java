package cinema.infrastructure.persistence.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
/** Mapping skeleton: remaining columns and constraints are pending. */
@Entity
@Table(name = "ticket_records")
public class TicketJpaEntity {
    @Id private String id;
    protected TicketJpaEntity() { }
    public String getId() { return id; }
}

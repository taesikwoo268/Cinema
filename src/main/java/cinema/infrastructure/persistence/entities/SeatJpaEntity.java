package cinema.infrastructure.persistence.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
/** Mapping skeleton: remaining columns and constraints are pending. */
@Entity
@Table(name = "seat_records")
public class SeatJpaEntity {
    @Id private String id;
    protected SeatJpaEntity() { }
    public String getId() { return id; }
}

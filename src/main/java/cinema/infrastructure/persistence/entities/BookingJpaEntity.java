package cinema.infrastructure.persistence.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
/** Mapping skeleton: remaining columns and constraints are pending. */
@Entity
@Table(name = "booking_records")
public class BookingJpaEntity {
    @Id private String id;
    protected BookingJpaEntity() { }
    public String getId() { return id; }
}

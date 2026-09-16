package cinema.infrastructure.persistence.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
/** Mapping skeleton: remaining columns and constraints are pending. */
@Entity
@Table(name = "screening_records")
public class ScreeningJpaEntity {
    @Id private String id;
    protected ScreeningJpaEntity() { }
    public String getId() { return id; }
}

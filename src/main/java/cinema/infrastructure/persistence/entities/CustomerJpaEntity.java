package cinema.infrastructure.persistence.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
/** Mapping skeleton: remaining columns and constraints are pending. */
@Entity
@Table(name = "customer_records")
public class CustomerJpaEntity {
    @Id private String id;
    protected CustomerJpaEntity() { }
    public String getId() { return id; }
}

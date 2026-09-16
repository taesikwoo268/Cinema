package cinema.infrastructure.persistence.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
/** Mapping skeleton: remaining columns and constraints are pending. */
@Entity
@Table(name = "cart_records")
public class CartJpaEntity {
    @Id private String id;
    protected CartJpaEntity() { }
    public String getId() { return id; }
}

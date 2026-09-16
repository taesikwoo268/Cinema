package cinema.infrastructure.persistence.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
/** Mapping skeleton: remaining columns and constraints are pending. */
@Entity
@Table(name = "payment_records")
public class PaymentJpaEntity {
    @Id private String id;
    protected PaymentJpaEntity() { }
    public String getId() { return id; }
}

package cinema.infrastructure.persistence.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "customer_records", uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id"})})
public class CustomerJpaEntity extends JpaRecord {
    @Column(name = "user_id", nullable = false, length = 128) public String userId;
    @Column(name = "display_name", nullable = false, length = 255) public String displayName;
}

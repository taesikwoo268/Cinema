package cinema.infrastructure.persistence.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "user_records", uniqueConstraints = {@UniqueConstraint(columnNames = {"email"})})
public class UserJpaEntity extends JpaRecord {
    @Column(name = "email", nullable = false, length = 255) public String email;
    @Column(name = "password_hash", nullable = false, length = 255) public String passwordHash;
}

package cinema.infrastructure.persistence.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "cinema_records")
public class CinemaJpaEntity extends JpaRecord {
    @Column(name = "name", nullable = false, length = 255) public String name;
    @Column(name = "address", nullable = false, length = 500) public String address;
}

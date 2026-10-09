package cinema.infrastructure.persistence.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "hall_records", uniqueConstraints = {@UniqueConstraint(columnNames = {"cinema_id", "name"})})
public class HallJpaEntity extends JpaRecord {
    @Column(name = "cinema_id", nullable = false, length = 128) public String cinemaId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cinema_id", insertable = false, updatable = false)
    private CinemaJpaEntity cinema;
    @Column(name = "name", nullable = false, length = 255) public String name;
}

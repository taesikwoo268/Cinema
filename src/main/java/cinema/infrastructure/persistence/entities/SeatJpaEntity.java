package cinema.infrastructure.persistence.entities;

import jakarta.persistence.*;
import cinema.domain.cinema.SeatType;

@Entity
@Table(name = "seat_records", uniqueConstraints = {@UniqueConstraint(columnNames = {"hall_id", "seat_row", "seat_number"})})
public class SeatJpaEntity extends JpaRecord {
    @Column(name = "hall_id", nullable = false, length = 128) public String hallId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hall_id", insertable = false, updatable = false)
    private HallJpaEntity hall;
    @Column(name = "seat_row", nullable = false, length = 32) public String row;
    @Column(name = "seat_number", nullable = false) public int number;
    @Enumerated(EnumType.STRING)
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.VARCHAR)
    @Column(name = "seat_type", nullable = false, length = 32) public SeatType type;
}

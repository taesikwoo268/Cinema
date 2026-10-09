package cinema.infrastructure.persistence.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "seat_hold_records", uniqueConstraints = {@UniqueConstraint(columnNames = {"screening_id", "seat_id"})})
public class SeatHoldJpaEntity extends JpaRecord {
    @Column(name = "screening_id", nullable = false, length = 128) public String screeningId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "screening_id", insertable = false, updatable = false)
    private ScreeningJpaEntity screening;
    @Column(name = "seat_id", nullable = false, length = 128) public String seatId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seat_id", insertable = false, updatable = false)
    private SeatJpaEntity seat;
    @Column(name = "customer_id", nullable = false, length = 128) public String customerId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", insertable = false, updatable = false)
    private CustomerJpaEntity customer;
}

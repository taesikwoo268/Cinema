package cinema.infrastructure.persistence.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "seat_reservation_records", uniqueConstraints = {@UniqueConstraint(columnNames = {"screening_id", "seat_id"})})
public class SeatReservationJpaEntity extends JpaRecord {
    @Column(name = "screening_id", nullable = false, length = 128) public String screeningId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "screening_id", insertable = false, updatable = false)
    private ScreeningJpaEntity screening;
    @Column(name = "seat_id", nullable = false, length = 128) public String seatId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seat_id", insertable = false, updatable = false)
    private SeatJpaEntity seat;
    @Column(name = "booking_id", nullable = false, length = 128) public String bookingId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", insertable = false, updatable = false)
    private BookingJpaEntity booking;
}

package cinema.infrastructure.persistence.entities;

import jakarta.persistence.*;
import cinema.domain.ticket.TicketStatus;

@Entity
@Table(name = "ticket_records", uniqueConstraints = {@UniqueConstraint(columnNames = {"ticket_code"}), @UniqueConstraint(columnNames = {"booking_id", "seat_id"})})
public class TicketJpaEntity extends JpaRecord {
    @Column(name = "booking_id", nullable = false, length = 128) public String bookingId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", insertable = false, updatable = false)
    private BookingJpaEntity booking;
    @Column(name = "screening_id", nullable = false, length = 128) public String screeningId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "screening_id", insertable = false, updatable = false)
    private ScreeningJpaEntity screening;
    @Column(name = "seat_id", nullable = false, length = 128) public String seatId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seat_id", insertable = false, updatable = false)
    private SeatJpaEntity seat;
    @Column(name = "ticket_code", nullable = false, length = 192) public String ticketCode;
    @Enumerated(EnumType.STRING)
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.VARCHAR)
    @Column(name = "status", nullable = false, length = 32) public TicketStatus status;
}

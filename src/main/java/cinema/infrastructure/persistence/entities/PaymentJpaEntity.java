package cinema.infrastructure.persistence.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import cinema.domain.payment.PaymentStatus;

@Entity
@Table(name = "payment_records")
public class PaymentJpaEntity extends JpaRecord {
    @Column(name = "successful_booking_id", length = 128, unique = true) public String successfulBookingId;
    @Column(name = "booking_id", nullable = false, length = 128) public String bookingId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", insertable = false, updatable = false)
    private BookingJpaEntity booking;
    @Column(name = "amount", nullable = false, precision = 38, scale = 12) public BigDecimal amount;
    @Column(name = "currency", nullable = false, length = 3) public String currency;
    @Enumerated(EnumType.STRING)
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.VARCHAR)
    @Column(name = "status", nullable = false, length = 32) public PaymentStatus status;
    @Column(name = "provider_reference", nullable = true, length = 255) public String providerReference;
}

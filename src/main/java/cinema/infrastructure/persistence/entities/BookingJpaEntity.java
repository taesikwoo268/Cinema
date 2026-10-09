package cinema.infrastructure.persistence.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import cinema.domain.booking.BookingStatus;

@Entity
@Table(name = "booking_records")
public class BookingJpaEntity extends JpaRecord {
    @Column(name = "customer_id", nullable = false, length = 128) public String customerId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", insertable = false, updatable = false)
    private CustomerJpaEntity customer;
    @Column(name = "screening_id", nullable = false, length = 128) public String screeningId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "screening_id", insertable = false, updatable = false)
    private ScreeningJpaEntity screening;
    @Column(name = "amount", nullable = false, precision = 38, scale = 12) public BigDecimal amount;
    @Column(name = "currency", nullable = false, length = 3) public String currency;
    @Enumerated(EnumType.STRING)
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.VARCHAR)
    @Column(name = "status", nullable = false, length = 32) public BookingStatus status;
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.TIMESTAMP)
    @Column(name = "created_at", nullable = false, columnDefinition = "datetime(6)") public Instant createdAt;
    @ElementCollection
    @CollectionTable(name = "booking_items", joinColumns = @JoinColumn(name = "booking_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"booking_id", "seat_id"}))
    @OrderColumn(name = "item_index")
    public List<BookingItemJpaValue> items = new ArrayList<>();
}

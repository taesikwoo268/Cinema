package cinema.infrastructure.persistence.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import cinema.domain.screening.ScreeningStatus;

@Entity
@Table(name = "screening_records")
public class ScreeningJpaEntity extends JpaRecord {
    @Column(name = "movie_id", nullable = false, length = 128) public String movieId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "movie_id", insertable = false, updatable = false)
    private MovieJpaEntity movie;
    @Column(name = "hall_id", nullable = false, length = 128) public String hallId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hall_id", insertable = false, updatable = false)
    private HallJpaEntity hall;
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.TIMESTAMP)
    @Column(name = "start_time", nullable = false, columnDefinition = "datetime(6)") public Instant startTime;
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.TIMESTAMP)
    @Column(name = "end_time", nullable = false, columnDefinition = "datetime(6)") public Instant endTime;
    @Column(name = "amount", nullable = false, precision = 38, scale = 12) public BigDecimal amount;
    @Column(name = "currency", nullable = false, length = 3) public String currency;
    @Enumerated(EnumType.STRING)
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.VARCHAR)
    @Column(name = "status", nullable = false, length = 32) public ScreeningStatus status;
}

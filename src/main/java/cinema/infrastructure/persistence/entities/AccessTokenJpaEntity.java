package cinema.infrastructure.persistence.entities;

import jakarta.persistence.*;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "access_token_records")
public class AccessTokenJpaEntity {
    @Id @Column(name = "token_hash", nullable = false, length = 64)
    public String tokenHash;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    public UserJpaEntity user;
    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    @Column(name = "expires_at", nullable = false)
    public Instant expiresAt;
}

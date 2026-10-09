package cinema.infrastructure.persistence.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Embeddable
public class CartItemJpaValue {
    @Column(name = "screening_id", nullable = false, length = 128) public String screeningId;
    @Column(name = "seat_id", nullable = false, length = 128) public String seatId;
    @Column(nullable = false, precision = 38, scale = 12) public BigDecimal amount;
    @Column(nullable = false, length = 3) public String currency;
}

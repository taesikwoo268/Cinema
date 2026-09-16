package cinema.application.common.models;

import java.time.Instant;
import java.math.BigDecimal;
import java.util.List;
public record CartItemView(String screeningId, String seatId, BigDecimal unitPrice, String currency) { }

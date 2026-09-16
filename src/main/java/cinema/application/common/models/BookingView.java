package cinema.application.common.models;

import java.time.Instant;
import java.math.BigDecimal;
import java.util.List;
public record BookingView(String id, String screeningId, String status, BigDecimal totalAmount, String currency) { }

package cinema.application.common.models;

import java.time.Instant;
import java.math.BigDecimal;
import java.util.List;
public record ScreeningView(String id, String movieId, String hallId, Instant startTime, Instant endTime, BigDecimal basePrice, String currency, String status) { }

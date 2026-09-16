package cinema.application.common.models;

import java.time.Instant;
import java.math.BigDecimal;
import java.util.List;
public record SeatView(String id, String row, int number, String type, boolean available) { }

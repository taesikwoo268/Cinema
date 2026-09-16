package cinema.application.common.models;

import java.time.Instant;
import java.math.BigDecimal;
import java.util.List;
public record CartView(String id, String customerId, List<CartItemView> items) { }

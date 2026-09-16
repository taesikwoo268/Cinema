package cinema.application.common.models;

import java.time.Instant;
import java.math.BigDecimal;
import java.util.List;
public record CurrentUserView(String userId, boolean authenticated) { }

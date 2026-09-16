package cinema.application.common.models;

import java.time.Instant;
import java.math.BigDecimal;
import java.util.List;
public record PaymentResult(boolean successful, String providerReference) { }

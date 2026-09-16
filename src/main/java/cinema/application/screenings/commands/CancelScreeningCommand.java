package cinema.application.screenings.commands;

import java.time.Instant;
import java.math.BigDecimal;
/** Input contract only. Validation is pending. */
public record CancelScreeningCommand(String screeningId) { }

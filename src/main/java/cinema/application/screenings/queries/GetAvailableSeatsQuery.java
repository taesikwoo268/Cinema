package cinema.application.screenings.queries;

import java.time.Instant;
import java.math.BigDecimal;
/** Input contract only. Validation is pending. */
public record GetAvailableSeatsQuery(String screeningId) { }

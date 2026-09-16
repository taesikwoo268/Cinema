package cinema.application.carts.commands;

import java.time.Instant;
import java.math.BigDecimal;
/** Input contract only. Validation is pending. */
public record RemoveSeatFromCartCommand(String screeningId, String seatId) { }

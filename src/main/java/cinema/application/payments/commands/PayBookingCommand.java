package cinema.application.payments.commands;

import java.time.Instant;
import java.math.BigDecimal;
/** Input contract only. Validation is pending. */
public record PayBookingCommand(String bookingId) { }

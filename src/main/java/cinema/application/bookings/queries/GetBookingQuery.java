package cinema.application.bookings.queries;

import java.time.Instant;
import java.math.BigDecimal;
/** Input contract only. Validation is pending. */
public record GetBookingQuery(String bookingId) { }

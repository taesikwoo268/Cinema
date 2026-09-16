package cinema.application.common.models;

import java.time.Instant;
import java.math.BigDecimal;
import java.util.List;
public record TicketView(String id, String bookingId, String screeningId, String seatId, String ticketCode, String status) { }

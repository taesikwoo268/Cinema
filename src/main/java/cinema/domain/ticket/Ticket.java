package cinema.domain.ticket;

import cinema.domain.common.BaseEntity;
import cinema.domain.common.Money;
import java.time.Instant;
import java.util.List;

/** Domain state skeleton; invariants and transitions are intentionally pending. */
public final class Ticket extends BaseEntity {
    private final String bookingId;
    private final String screeningId;
    private final String seatId;
    private final String ticketCode;
    private final TicketStatus status;
    public Ticket(String id, String bookingId, String screeningId, String seatId, String ticketCode, TicketStatus status) {
        super(id);
        this.bookingId = bookingId;
        this.screeningId = screeningId;
        this.seatId = seatId;
        this.ticketCode = ticketCode;
        this.status = status;
    }
    public String getBookingId() { return bookingId; }
    public String getScreeningId() { return screeningId; }
    public String getSeatId() { return seatId; }
    public String getTicketCode() { return ticketCode; }
    public TicketStatus getStatus() { return status; }
    public void markUsed() { throw new UnsupportedOperationException("Architecture Contract v0.1: not implemented" ); }
    public void cancel() { throw new UnsupportedOperationException("Architecture Contract v0.1: not implemented" ); }
}

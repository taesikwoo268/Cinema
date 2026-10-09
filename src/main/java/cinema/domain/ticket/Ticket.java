package cinema.domain.ticket;

import cinema.domain.common.BaseEntity;
import cinema.domain.common.DomainException;

public final class Ticket extends BaseEntity {
    private final String bookingId;
    private final String screeningId;
    private final String seatId;
    private final String ticketCode;
    private TicketStatus status;

    public Ticket(String id, String bookingId, String screeningId, String seatId, String ticketCode, TicketStatus status) {
        super(id);
        requireText(bookingId, "bookingId");
        requireText(screeningId, "screeningId");
        requireText(seatId, "seatId");
        requireText(ticketCode, "ticketCode");
        if (status == null) throw new DomainException("Ticket status must not be null");
        this.bookingId = bookingId;
        this.screeningId = screeningId;
        this.seatId = seatId;
        this.ticketCode = ticketCode;
        this.status = status;
    }
    private static void requireText(String value, String name) {
        if (value == null || value.isBlank()) throw new DomainException(name + " must not be blank");
    }
    public String getBookingId() { return bookingId; }
    public String getScreeningId() { return screeningId; }
    public String getSeatId() { return seatId; }
    public String getTicketCode() { return ticketCode; }
    public TicketStatus getStatus() { return status; }
    public void markUsed() { transition(TicketStatus.USED); }
    public void cancel() { transition(TicketStatus.CANCELLED); }
    private void transition(TicketStatus next) {
        if (status != TicketStatus.VALID) throw new DomainException("Only valid tickets can transition");
        status = next;
    }
}

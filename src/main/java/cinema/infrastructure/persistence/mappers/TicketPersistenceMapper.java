package cinema.infrastructure.persistence.mappers;

import cinema.domain.ticket.Ticket;
import cinema.domain.common.Money;
import cinema.infrastructure.persistence.entities.*;
import java.util.Currency;

public final class TicketPersistenceMapper {
    public Ticket toDomain(TicketJpaEntity e) {
        return new Ticket(e.id, e.bookingId, e.screeningId, e.seatId, e.ticketCode, e.status);
    }
    public TicketJpaEntity toEntity(Ticket model) {
        var e = new TicketJpaEntity();
        e.id = model.getId();
        e.bookingId = model.getBookingId();
        e.screeningId = model.getScreeningId();
        e.seatId = model.getSeatId();
        e.ticketCode = model.getTicketCode();
        e.status = model.getStatus();
        return e;
    }
}

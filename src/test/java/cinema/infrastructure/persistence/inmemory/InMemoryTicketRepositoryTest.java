package cinema.infrastructure.persistence.inmemory;

import cinema.application.common.exceptions.ConflictException;
import cinema.domain.ticket.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryTicketRepositoryTest {
    final InMemoryTicketRepository tickets = new InMemoryTicketRepository(new InMemoryBookingRepository());
    Ticket ticket(String id,String booking,String seat,String code) { return new Ticket(id,booking,"screen",seat,code,TicketStatus.VALID); }
    @Test void duplicateSaveIsIdempotentAndRetainsState() {
        var t=ticket("t","b","a","code"); tickets.save(t); t.markUsed();
        tickets.save(ticket("t","b","a","code"));
        assertEquals(TicketStatus.USED,tickets.findById("t").orElseThrow().getStatus());
        assertTrue(tickets.findById("missing").isEmpty());
    }
    @Test void enforcesUniqueIdCodeAndBookingSeat() {
        tickets.save(ticket("t","b","a","code"));
        assertThrows(ConflictException.class, () -> tickets.save(ticket("t2","b","a","code2")));
        assertThrows(ConflictException.class, () -> tickets.save(ticket("t","b2","a2","code2")));
        assertThrows(ConflictException.class, () -> tickets.save(ticket("t2","b2","a2","code")));
        assertThrows(NullPointerException.class, () -> tickets.save(null));
        tickets.save(ticket("t2","b2","a","code2"));
        assertTrue(tickets.findById("t2").isPresent());
    }
}

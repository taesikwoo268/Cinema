package cinema.domain.ticket;

import cinema.domain.common.DomainException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class TicketTest {
    private Ticket ticket(TicketStatus status) { return new Ticket("t", "b", "screen", "seat", "code", status); }
    @ParameterizedTest @NullAndEmptySource @ValueSource(strings = {" ", "\t\n"})
    void rejectsEveryBlankIdentifierAndCode(String value) {
        assertThrows(DomainException.class, () -> new Ticket(value,"b","s","a","code",TicketStatus.VALID));
        assertThrows(DomainException.class, () -> new Ticket("t",value,"s","a","code",TicketStatus.VALID));
        assertThrows(DomainException.class, () -> new Ticket("t","b",value,"a","code",TicketStatus.VALID));
        assertThrows(DomainException.class, () -> new Ticket("t","b","s",value,"code",TicketStatus.VALID));
        assertThrows(DomainException.class, () -> new Ticket("t","b","s","a",value,TicketStatus.VALID));
    }
    @Test void rejectsNullStatus() { assertThrows(DomainException.class, () -> ticket(null)); }
    @Test void validCanBeUsed() { var t=ticket(TicketStatus.VALID); t.markUsed(); assertEquals(TicketStatus.USED,t.getStatus()); }
    @Test void validCanBeCancelled() { var t=ticket(TicketStatus.VALID); t.cancel(); assertEquals(TicketStatus.CANCELLED,t.getStatus()); }
    @ParameterizedTest @EnumSource(value=TicketStatus.class,names={"USED","CANCELLED"})
    void terminalStatesRejectEveryTransition(TicketStatus status) {
        var t=ticket(status);
        assertThrows(DomainException.class,t::markUsed);
        assertThrows(DomainException.class,t::cancel);
        assertEquals(status,t.getStatus());
    }
}

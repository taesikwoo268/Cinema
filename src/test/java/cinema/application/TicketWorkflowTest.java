package cinema.application;

import cinema.application.common.interfaces.*;
import cinema.application.common.exceptions.*;
import cinema.application.common.models.PaymentResult;
import cinema.application.payments.commands.*;
import cinema.application.tickets.queries.*;
import cinema.application.bookings.commands.*;
import cinema.domain.booking.*;
import cinema.domain.common.*;
import cinema.domain.customer.Customer;
import cinema.domain.ticket.*;
import cinema.infrastructure.persistence.inmemory.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TicketWorkflowTest {
    final CurrentUser user = mock(CurrentUser.class);
    final InMemoryCustomerRepository customers = new InMemoryCustomerRepository();
    final InMemoryBookingRepository bookings = new InMemoryBookingRepository();
    final InMemoryPaymentRepository payments = new InMemoryPaymentRepository();
    final InMemoryTicketRepository tickets = new InMemoryTicketRepository(bookings);
    final InMemorySeatReservationRepository reservations = new InMemorySeatReservationRepository();
    final PaymentGateway gateway = mock(PaymentGateway.class);
    final Money price = new Money(BigDecimal.TEN, Currency.getInstance("USD"));
    final Booking booking = new Booking("b","c","screen",List.of(new BookingItem("a",price),new BookingItem("b",price)),
            new Money(BigDecimal.valueOf(20),Currency.getInstance("USD")),BookingStatus.PENDING,Instant.now());
    PayBookingCommandHandler handler(TicketRepository repo) {
        return new PayBookingCommandHandler(user, customers, bookings, payments, gateway, () -> UUID.randomUUID().toString(), repo, reservations);
    }
    @BeforeEach void setup() {
        customers.save(new Customer("c","u","Owner"));
        customers.save(new Customer("other","other-user","Other"));
        bookings.save(booking);
        when(user.isAuthenticated()).thenReturn(true);
        when(user.getUserId()).thenReturn("u");
        when(gateway.pay("b",booking.getTotalAmount())).thenReturn(new PaymentResult(true,"ref"));
    }
    @Test void successIssuesOneTicketPerItemAndRetriesDoNotChargeOrResetUsedTickets() {
        var handler=handler(tickets);
        var result=handler.handle(new PayBookingCommand("b"));
        var issued=tickets.findByCustomerId("c");
        assertEquals(2,issued.size());
        issued.getFirst().markUsed();
        assertEquals(result,handler.handle(new PayBookingCommand("b")));
        assertEquals(BookingStatus.CONFIRMED,booking.getStatus());
        assertEquals(2,tickets.findByCustomerId("c").size());
        assertEquals(TicketStatus.USED,issued.getFirst().getStatus());
        assertTrue(reservations.isBooked("screen","a"));
        assertTrue(reservations.isBooked("screen","b"));
        assertEquals(1,payments.findByBookingId("b").size());
        verify(gateway,times(1)).pay("b",booking.getTotalAmount());
        assertThrows(ConflictException.class, () -> new CancelBookingCommandHandler(user,customers,bookings,new SimpleSeatHoldService()).handle(new CancelBookingCommand("b")));
        assertTrue(reservations.isBooked("screen","a"));
    }
    @Test void failureNeverIssuesOrReservesAndCanBeRetried() {
        when(gateway.pay("b",booking.getTotalAmount())).thenReturn(new PaymentResult(false,"declined"),new PaymentResult(true,"ref"));
        assertFalse(handler(tickets).handle(new PayBookingCommand("b")).successful());
        assertEquals(BookingStatus.PENDING,booking.getStatus());
        assertTrue(tickets.findByCustomerId("c").isEmpty());
        assertFalse(reservations.isBooked("screen","a"));
        assertTrue(handler(tickets).handle(new PayBookingCommand("b")).successful());
        assertEquals(2,tickets.findByCustomerId("c").size());
    }
    @Test void retryRecoversPartialTicketWriteWithoutRecharging() {
        TicketRepository flaky=spy(tickets);
        doCallRealMethod().doThrow(new IllegalStateException("write failed")).when(flaky).save(any());
        assertThrows(IllegalStateException.class, () -> handler(flaky).handle(new PayBookingCommand("b")));
        assertTrue(reservations.isBooked("screen","a"));
        assertEquals(BookingStatus.CONFIRMED,booking.getStatus());
        handler(tickets).handle(new PayBookingCommand("b"));
        assertEquals(2,tickets.findByCustomerId("c").size());
        verify(gateway,times(1)).pay("b",booking.getTotalAmount());
    }
    @Test void simultaneousRetriesIssueOnlyOneSet() throws Exception {
        try (var pool=Executors.newFixedThreadPool(2)) {
            var start=new CountDownLatch(1);
            Callable<PaymentResult> call=() -> {start.await(); return handler(tickets).handle(new PayBookingCommand("b"));};
            var first=pool.submit(call); var second=pool.submit(call); start.countDown();
            assertEquals(first.get(5,TimeUnit.SECONDS),second.get(5,TimeUnit.SECONDS));
        }
        assertEquals(2,tickets.findByCustomerId("c").size());
        verify(gateway,times(1)).pay("b",booking.getTotalAmount());
    }
    @Test void anotherBookingCannotChargeForAlreadyPaidSeat() {
        handler(tickets).handle(new PayBookingCommand("b"));
        bookings.save(new Booking("duplicate","c","screen",booking.getItems(),booking.getTotalAmount(),BookingStatus.PENDING,Instant.now()));
        assertThrows(ConflictException.class, () -> handler(tickets).handle(new PayBookingCommand("duplicate")));
        verify(gateway,never()).pay(eq("duplicate"),any());
    }
    @Test void ticketQueriesAndPaymentEnforceOwnershipAndAuthentication() {
        handler(tickets).handle(new PayBookingCommand("b"));
        String id=tickets.findByCustomerId("c").getFirst().getId();
        var get=new GetTicketQueryHandler(user,customers,bookings,tickets);
        var mine=new GetMyTicketsQueryHandler(user,customers,bookings,tickets);
        assertEquals(id,get.handle(new GetTicketQuery(id)).id());
        assertEquals(2,mine.handle(new GetMyTicketsQuery()).size());
        when(user.getUserId()).thenReturn("other-user");
        assertThrows(ForbiddenException.class, () -> get.handle(new GetTicketQuery(id)));
        assertTrue(mine.handle(new GetMyTicketsQuery()).isEmpty());
        assertThrows(ForbiddenException.class, () -> handler(tickets).handle(new PayBookingCommand("b")));
        when(user.isAuthenticated()).thenReturn(false);
        assertThrows(ForbiddenException.class, () -> get.handle(new GetTicketQuery(id)));
        assertThrows(ForbiddenException.class, () -> mine.handle(new GetMyTicketsQuery()));
        assertThrows(ForbiddenException.class, () -> handler(tickets).handle(new PayBookingCommand("b")));
    }
    @Test void queriesValidateInputAndMissingResources() {
        var get=new GetTicketQueryHandler(user,customers,bookings,tickets);
        var mine=new GetMyTicketsQueryHandler(user,customers,bookings,tickets);
        assertThrows(ValidationException.class, () -> get.handle(null));
        assertThrows(ValidationException.class, () -> get.handle(new GetTicketQuery(" ")));
        assertThrows(ValidationException.class, () -> mine.handle(null));
        assertThrows(NotFoundException.class, () -> get.handle(new GetTicketQuery("missing")));
        when(user.getUserId()).thenReturn(" ");
        assertThrows(ForbiddenException.class, () -> mine.handle(new GetMyTicketsQuery()));
        when(user.getUserId()).thenReturn("missing");
        assertThrows(NotFoundException.class, () -> mine.handle(new GetMyTicketsQuery()));
    }
    @Test void listChecksBookingOwnerEvenIfTicketPortReturnsForeignTickets() {
        TicketRepository untrusted=mock(TicketRepository.class);
        bookings.save(new Booking("foreign","other","screen",booking.getItems(),booking.getTotalAmount(),BookingStatus.CONFIRMED,Instant.now()));
        when(untrusted.findByCustomerId("c")).thenReturn(List.of(new Ticket("t","foreign","screen","a","code",TicketStatus.VALID)));
        assertTrue(new GetMyTicketsQueryHandler(user,customers,bookings,untrusted).handle(new GetMyTicketsQuery()).isEmpty());
    }
}

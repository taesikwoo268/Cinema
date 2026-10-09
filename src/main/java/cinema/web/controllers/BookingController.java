package cinema.web.controllers;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import cinema.application.bookings.commands.CancelBookingCommand;
import cinema.application.bookings.commands.CancelBookingCommandHandler;
import cinema.application.bookings.commands.CheckoutCartCommand;
import cinema.application.bookings.commands.CheckoutCartCommandHandler;
import cinema.application.bookings.queries.GetBookingQuery;
import cinema.application.bookings.queries.GetBookingQueryHandler;
import cinema.application.bookings.queries.GetMyBookingsQuery;
import cinema.application.bookings.queries.GetMyBookingsQueryHandler;
import cinema.application.common.models.BookingView;
import cinema.application.common.models.CheckoutResult;

/** HTTP endpoints for customer booking use cases. */
@RestController
@Profile({"venue-dev", "mysql"})
@RequestMapping("/bookings")
public final class BookingController {

    private final CheckoutCartCommandHandler checkoutCartCommand;
    private final CancelBookingCommandHandler cancelBookingCommand;
    private final GetMyBookingsQueryHandler getMyBookingsQuery;
    private final GetBookingQueryHandler getBookingQuery;

    public BookingController(CheckoutCartCommandHandler checkoutCartCommand, CancelBookingCommandHandler cancelBookingCommand, GetMyBookingsQueryHandler getMyBookingsQuery, GetBookingQueryHandler getBookingQuery) {
        this.checkoutCartCommand = checkoutCartCommand;
        this.cancelBookingCommand = cancelBookingCommand;
        this.getMyBookingsQuery = getMyBookingsQuery;
        this.getBookingQuery = getBookingQuery;
    }

    @PostMapping("/checkout")
    public CheckoutResult checkout() {
        return checkoutCartCommand.handle(new CheckoutCartCommand());
    }

    @PostMapping("/{id}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable String id) {
        cancelBookingCommand.handle(new CancelBookingCommand(id));
    }

    @GetMapping("/me")
    public List<BookingView> getMyBookings() {
        return getMyBookingsQuery.handle(new GetMyBookingsQuery());
    }

    @GetMapping("/{id}")
    public BookingView getBooking(@PathVariable String id) {
        return getBookingQuery.handle(new GetBookingQuery(id));
    }
}

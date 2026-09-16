package cinema.web.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import cinema.application.bookings.commands.CheckoutCartCommandHandler;
import cinema.application.bookings.commands.CancelBookingCommandHandler;
import cinema.application.bookings.queries.GetMyBookingsQueryHandler;
import cinema.application.bookings.queries.GetBookingQueryHandler;
/** HTTP methods, request mapping and response mapping are pending implementation. */
@RestController
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
}

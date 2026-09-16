package cinema.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/** Composition root. Replace stub wiring when each feature is implemented. */
@Configuration
public class UseCaseConfiguration {
    @Bean public cinema.application.movies.commands.CreateMovieCommandHandler createMovieCommandHandler() { return new cinema.application.movies.commands.CreateMovieCommandHandler(); }
    @Bean public cinema.application.movies.commands.DeleteMovieCommandHandler deleteMovieCommandHandler() { return new cinema.application.movies.commands.DeleteMovieCommandHandler(); }
    @Bean public cinema.application.screenings.commands.CreateScreeningCommandHandler createScreeningCommandHandler() { return new cinema.application.screenings.commands.CreateScreeningCommandHandler(); }
    @Bean public cinema.application.screenings.commands.CancelScreeningCommandHandler cancelScreeningCommandHandler() { return new cinema.application.screenings.commands.CancelScreeningCommandHandler(); }
    @Bean public cinema.application.carts.commands.AddSeatToCartCommandHandler addSeatToCartCommandHandler() { return new cinema.application.carts.commands.AddSeatToCartCommandHandler(); }
    @Bean public cinema.application.carts.commands.RemoveSeatFromCartCommandHandler removeSeatFromCartCommandHandler() { return new cinema.application.carts.commands.RemoveSeatFromCartCommandHandler(); }
    @Bean public cinema.application.carts.commands.ClearCartCommandHandler clearCartCommandHandler() { return new cinema.application.carts.commands.ClearCartCommandHandler(); }
    @Bean public cinema.application.bookings.commands.CheckoutCartCommandHandler checkoutCartCommandHandler() { return new cinema.application.bookings.commands.CheckoutCartCommandHandler(); }
    @Bean public cinema.application.bookings.commands.CancelBookingCommandHandler cancelBookingCommandHandler() { return new cinema.application.bookings.commands.CancelBookingCommandHandler(); }
    @Bean public cinema.application.payments.commands.PayBookingCommandHandler payBookingCommandHandler() { return new cinema.application.payments.commands.PayBookingCommandHandler(); }
    @Bean public cinema.application.identity.commands.RegisterCommandHandler registerCommandHandler() { return new cinema.application.identity.commands.RegisterCommandHandler(); }
    @Bean public cinema.application.identity.commands.LoginCommandHandler loginCommandHandler() { return new cinema.application.identity.commands.LoginCommandHandler(); }
    @Bean public cinema.application.movies.queries.GetMoviesQueryHandler getMoviesQueryHandler() { return new cinema.application.movies.queries.GetMoviesQueryHandler(); }
    @Bean public cinema.application.movies.queries.GetMovieQueryHandler getMovieQueryHandler() { return new cinema.application.movies.queries.GetMovieQueryHandler(); }
    @Bean public cinema.application.screenings.queries.GetScreeningsQueryHandler getScreeningsQueryHandler() { return new cinema.application.screenings.queries.GetScreeningsQueryHandler(); }
    @Bean public cinema.application.screenings.queries.GetScreeningQueryHandler getScreeningQueryHandler() { return new cinema.application.screenings.queries.GetScreeningQueryHandler(); }
    @Bean public cinema.application.screenings.queries.GetAvailableSeatsQueryHandler getAvailableSeatsQueryHandler() { return new cinema.application.screenings.queries.GetAvailableSeatsQueryHandler(); }
    @Bean public cinema.application.carts.queries.GetMyCartQueryHandler getMyCartQueryHandler() { return new cinema.application.carts.queries.GetMyCartQueryHandler(); }
    @Bean public cinema.application.bookings.queries.GetMyBookingsQueryHandler getMyBookingsQueryHandler() { return new cinema.application.bookings.queries.GetMyBookingsQueryHandler(); }
    @Bean public cinema.application.bookings.queries.GetBookingQueryHandler getBookingQueryHandler() { return new cinema.application.bookings.queries.GetBookingQueryHandler(); }
    @Bean public cinema.application.tickets.queries.GetMyTicketsQueryHandler getMyTicketsQueryHandler() { return new cinema.application.tickets.queries.GetMyTicketsQueryHandler(); }
    @Bean public cinema.application.tickets.queries.GetTicketQueryHandler getTicketQueryHandler() { return new cinema.application.tickets.queries.GetTicketQueryHandler(); }
    @Bean public cinema.application.identity.queries.GetCurrentUserQueryHandler getCurrentUserQueryHandler() { return new cinema.application.identity.queries.GetCurrentUserQueryHandler(); }
}

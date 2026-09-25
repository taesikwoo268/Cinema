package cinema.infrastructure.config;

import cinema.application.common.interfaces.MovieRepository;
import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
/** Composition root. Replace stub wiring when each feature is implemented. */
@Configuration
public class UseCaseConfiguration {
    @Bean @Profile("venue-dev") public cinema.application.movies.commands.CreateMovieCommandHandler createMovieCommandHandler(MovieRepository movieRepository) { return new cinema.application.movies.commands.CreateMovieCommandHandler(movieRepository, () -> UUID.randomUUID().toString()); }
    @Bean @Profile("venue-dev") public cinema.application.movies.commands.DeleteMovieCommandHandler deleteMovieCommandHandler(MovieRepository movieRepository) { return new cinema.application.movies.commands.DeleteMovieCommandHandler(movieRepository); }
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
    @Bean @Profile("venue-dev") public cinema.application.movies.queries.GetMoviesQueryHandler getMoviesQueryHandler(MovieRepository movieRepository) { return new cinema.application.movies.queries.GetMoviesQueryHandler(movieRepository); }
    @Bean @Profile("venue-dev") public cinema.application.movies.queries.GetMovieQueryHandler getMovieQueryHandler(MovieRepository movieRepository) { return new cinema.application.movies.queries.GetMovieQueryHandler(movieRepository); }
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

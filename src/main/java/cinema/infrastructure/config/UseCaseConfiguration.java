package cinema.infrastructure.config;

import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import cinema.application.common.interfaces.HallRepository;
import cinema.application.common.interfaces.MovieRepository;
import cinema.application.common.interfaces.ScreeningRepository;

/**
 * Composition root. Replace stub wiring when each feature is implemented.
 */
@Configuration
public class UseCaseConfiguration {

    @Bean
    @Profile("venue-dev")
    public cinema.application.movies.commands.CreateMovieCommandHandler createMovieCommandHandler(MovieRepository movieRepository) {
        return new cinema.application.movies.commands.CreateMovieCommandHandler(movieRepository, () -> UUID.randomUUID().toString());
    }

    @Bean
    @Profile("venue-dev")
    public cinema.application.movies.commands.DeleteMovieCommandHandler deleteMovieCommandHandler(MovieRepository movieRepository) {
        return new cinema.application.movies.commands.DeleteMovieCommandHandler(movieRepository);
    }

    @Bean
    @Profile("venue-dev")
    public cinema.application.screenings.commands.CreateScreeningCommandHandler createScreeningCommandHandler(ScreeningRepository screeningRepository, MovieRepository movieRepository, HallRepository hallRepository) {
        return new cinema.application.screenings.commands.CreateScreeningCommandHandler(screeningRepository, movieRepository, hallRepository, () -> UUID.randomUUID().toString());
    }

    @Bean
    @Profile("venue-dev")
    public cinema.application.screenings.commands.CancelScreeningCommandHandler cancelScreeningCommandHandler(ScreeningRepository screeningRepository) {
        return new cinema.application.screenings.commands.CancelScreeningCommandHandler(screeningRepository);
    }

    @Bean
    @Profile("venue-dev")
    public cinema.application.carts.commands.AddSeatToCartCommandHandler addSeatToCartCommandHandler(
            cinema.application.common.interfaces.CurrentUser currentUser,
            cinema.application.common.interfaces.CustomerRepository customerRepository,
            cinema.application.common.interfaces.ScreeningRepository screeningRepository,
            cinema.application.common.interfaces.SeatRepository seatRepository,
            cinema.application.common.interfaces.SeatReservationRepository seatReservationRepository,
            cinema.application.common.interfaces.SeatHoldService seatHoldService,
            cinema.application.common.interfaces.CartRepository cartRepository) {

        return new cinema.application.carts.commands.AddSeatToCartCommandHandler(
                currentUser,
                customerRepository,
                screeningRepository,
                seatRepository,
                seatReservationRepository,
                seatHoldService,
                cartRepository,
                () -> UUID.randomUUID().toString());
    }

    @Bean
    @Profile("venue-dev")
    public cinema.application.carts.commands.RemoveSeatFromCartCommandHandler removeSeatFromCartCommandHandler(
            cinema.application.common.interfaces.CurrentUser currentUser,
            cinema.application.common.interfaces.CustomerRepository customerRepository,
            cinema.application.common.interfaces.CartRepository cartRepository,
            cinema.application.common.interfaces.SeatHoldService seatHoldService) {

        return new cinema.application.carts.commands.RemoveSeatFromCartCommandHandler(
                currentUser,
                customerRepository,
                cartRepository,
                seatHoldService);
    }

    @Bean
    @Profile("venue-dev")
    public cinema.application.carts.commands.ClearCartCommandHandler clearCartCommandHandler(
            cinema.application.common.interfaces.CurrentUser currentUser,
            cinema.application.common.interfaces.CustomerRepository customerRepository,
            cinema.application.common.interfaces.CartRepository cartRepository,
            cinema.application.common.interfaces.SeatHoldService seatHoldService) {

        return new cinema.application.carts.commands.ClearCartCommandHandler(
                currentUser,
                customerRepository,
                cartRepository,
                seatHoldService);
    }

    @Bean
    public cinema.application.bookings.commands.CheckoutCartCommandHandler checkoutCartCommandHandler() {
        return new cinema.application.bookings.commands.CheckoutCartCommandHandler();
    }

    @Bean
    public cinema.application.bookings.commands.CancelBookingCommandHandler cancelBookingCommandHandler() {
        return new cinema.application.bookings.commands.CancelBookingCommandHandler();
    }

    @Bean
    public cinema.application.payments.commands.PayBookingCommandHandler payBookingCommandHandler() {
        return new cinema.application.payments.commands.PayBookingCommandHandler();
    }

    @Bean
    @Profile("venue-dev")
    public cinema.application.identity.commands.RegisterCommandHandler registerCommandHandler(
            cinema.application.common.interfaces.IdentityService identityService,
            cinema.application.common.interfaces.CustomerRepository customerRepository) {
        return new cinema.application.identity.commands.RegisterCommandHandler(
                identityService,
                customerRepository,
                () -> UUID.randomUUID().toString());
    }

    @Bean
    public cinema.application.common.interfaces.IdentityService identityService() {
        return new cinema.infrastructure.identity.IdentityServiceImpl();
    }

    @Bean
    public cinema.application.identity.commands.LoginCommandHandler loginCommandHandler(
            cinema.application.common.interfaces.IdentityService identityService) {
        return new cinema.application.identity.commands.LoginCommandHandler(identityService);
    }

    @Bean
    @Profile("venue-dev")
    public cinema.application.movies.queries.GetMoviesQueryHandler getMoviesQueryHandler(MovieRepository movieRepository) {
        return new cinema.application.movies.queries.GetMoviesQueryHandler(movieRepository);
    }

    @Bean
    @Profile("venue-dev")
    public cinema.application.movies.queries.GetMovieQueryHandler getMovieQueryHandler(MovieRepository movieRepository) {
        return new cinema.application.movies.queries.GetMovieQueryHandler(movieRepository);
    }

    @Bean
    @Profile("venue-dev")
    public cinema.application.screenings.queries.GetScreeningsQueryHandler getScreeningsQueryHandler(ScreeningRepository screeningRepository) {
        return new cinema.application.screenings.queries.GetScreeningsQueryHandler(screeningRepository);
    }

    @Bean
    @Profile("venue-dev")
    public cinema.application.screenings.queries.GetScreeningQueryHandler getScreeningQueryHandler(ScreeningRepository screeningRepository) {
        return new cinema.application.screenings.queries.GetScreeningQueryHandler(screeningRepository);
    }

    @Bean
    public cinema.application.screenings.queries.GetAvailableSeatsQueryHandler getAvailableSeatsQueryHandler() {
        return new cinema.application.screenings.queries.GetAvailableSeatsQueryHandler();
    }

    @Bean
    @Profile("venue-dev")
    public cinema.application.carts.queries.GetMyCartQueryHandler getMyCartQueryHandler(
            cinema.application.common.interfaces.CurrentUser currentUser,
            cinema.application.common.interfaces.CustomerRepository customerRepository,
            cinema.application.common.interfaces.CartRepository cartRepository) {

        return new cinema.application.carts.queries.GetMyCartQueryHandler(
                currentUser,
                customerRepository,
                cartRepository);
    }

    @Bean
    public cinema.application.bookings.queries.GetMyBookingsQueryHandler getMyBookingsQueryHandler() {
        return new cinema.application.bookings.queries.GetMyBookingsQueryHandler();
    }

    @Bean
    public cinema.application.bookings.queries.GetBookingQueryHandler getBookingQueryHandler() {
        return new cinema.application.bookings.queries.GetBookingQueryHandler();
    }

    @Bean
    public cinema.application.tickets.queries.GetMyTicketsQueryHandler getMyTicketsQueryHandler() {
        return new cinema.application.tickets.queries.GetMyTicketsQueryHandler();
    }

    @Bean
    public cinema.application.tickets.queries.GetTicketQueryHandler getTicketQueryHandler() {
        return new cinema.application.tickets.queries.GetTicketQueryHandler();
    }

    @Bean
    public cinema.application.identity.queries.GetCurrentUserQueryHandler getCurrentUserQueryHandler(
            cinema.application.common.interfaces.CurrentUser currentUser) {
        return new cinema.application.identity.queries.GetCurrentUserQueryHandler(currentUser);
    }
}

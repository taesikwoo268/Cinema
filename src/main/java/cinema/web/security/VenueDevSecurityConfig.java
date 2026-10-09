package cinema.web.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

/**
 * Development-only security configuration for venue-dev routes.
 */
@Configuration
@Profile("venue-dev & !mysql")
public class VenueDevSecurityConfig {

    @Bean
    SecurityFilterChain venueDevSecurityFilterChain(HttpSecurity http) throws Exception {
        var paths = PathPatternRequestMatcher.withDefaults();
        var createCinema = paths.matcher(HttpMethod.POST, "/cinemas");
        var createHall = paths.matcher(HttpMethod.POST, "/halls");
        var createSeat = paths.matcher(HttpMethod.POST, "/seats");

        var createMovie = paths.matcher(HttpMethod.POST, "/movies");
        var deleteMovie = paths.matcher(HttpMethod.DELETE, "/movies/{id}");
        var createScreening = paths.matcher(HttpMethod.POST, "/screenings");
        var getScreenings = paths.matcher(HttpMethod.GET, "/screenings");
        var getScreening = paths.matcher(HttpMethod.GET, "/screenings/{id}");
        var cancelScreening = paths.matcher(HttpMethod.POST, "/screenings/{id}/cancel");
        var register = paths.matcher(HttpMethod.POST, "/identity/register");
        var login = paths.matcher(HttpMethod.POST, "/identity/login");
        var currentUser = paths.matcher(HttpMethod.GET, "/identity/me");
        var addCartSeat = paths.matcher(HttpMethod.POST, "/carts/seats");
        var removeCartSeat = paths.matcher(HttpMethod.DELETE, "/carts/seats");
        var clearCart = paths.matcher(HttpMethod.DELETE, "/carts");
        var getMyCart = paths.matcher(HttpMethod.GET, "/carts/me");
        var checkoutBooking = paths.matcher(HttpMethod.POST, "/bookings/checkout");
        var cancelBooking = paths.matcher(HttpMethod.POST, "/bookings/{id}/cancel");
        var getMyBookings = paths.matcher(HttpMethod.GET, "/bookings/me");
        var getBooking = paths.matcher(HttpMethod.GET, "/bookings/{id}");
        var payBooking = paths.matcher(HttpMethod.POST, "/payments/{bookingId}");

        return http
                .csrf(csrf -> csrf.ignoringRequestMatchers(
                createCinema,
                createHall,
                createSeat,
                createMovie,
                deleteMovie,
                createScreening,
                cancelScreening,
                register,
                login,
                addCartSeat,
                removeCartSeat,
                clearCart,
                checkoutBooking,
                cancelBooking,
                payBooking))
                .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        createCinema,
                        createHall,
                        createSeat,
                        createMovie,
                        deleteMovie,
                        createScreening,
                        cancelScreening,
                        register,
                        login,
                        currentUser,
                        addCartSeat,
                        removeCartSeat,
                        clearCart,
                        getMyCart,
                        checkoutBooking,
                        cancelBooking,
                        getMyBookings,
                        getBooking,
                        payBooking,
                        paths.matcher(HttpMethod.GET, "/tickets/me"),
                        paths.matcher(HttpMethod.GET, "/tickets/{id}")
                ).permitAll()
                .requestMatchers(
                        paths.matcher(HttpMethod.GET, "/cinemas"),
                        paths.matcher(HttpMethod.GET, "/cinemas/{id}"),
                        paths.matcher(HttpMethod.GET, "/cinemas/{id}/halls"),
                        paths.matcher(HttpMethod.GET, "/halls/{id}"),
                        paths.matcher(HttpMethod.GET, "/halls/{id}/seats"),
                        paths.matcher(HttpMethod.GET, "/movies"),
                        paths.matcher(HttpMethod.GET, "/movies/{id}"),
                        getScreenings,
                        getScreening,
                        paths.matcher(HttpMethod.GET, "/screenings/{id}/available-seats")
                ).permitAll()
                .anyRequest().denyAll())
                .build();
    }
}

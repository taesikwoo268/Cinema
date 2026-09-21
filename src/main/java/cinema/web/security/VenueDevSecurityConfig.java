package cinema.web.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

/** Development-only access to the M1 venue routes; other routes remain denied. */
@Configuration
@Profile("venue-dev")
public class VenueDevSecurityConfig {
    @Bean
    SecurityFilterChain venueDevSecurityFilterChain(HttpSecurity http) throws Exception {
        var paths = PathPatternRequestMatcher.withDefaults();
        var createCinema = paths.matcher(HttpMethod.POST, "/cinemas");
        var createHall = paths.matcher(HttpMethod.POST, "/halls");
        var createSeat = paths.matcher(HttpMethod.POST, "/seats");

        return http
            .csrf(csrf -> csrf.ignoringRequestMatchers(createCinema, createHall, createSeat))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(createCinema, createHall, createSeat).permitAll()
                .requestMatchers(
                    paths.matcher(HttpMethod.GET, "/cinemas"),
                    paths.matcher(HttpMethod.GET, "/cinemas/{id}"),
                    paths.matcher(HttpMethod.GET, "/cinemas/{id}/halls"),
                    paths.matcher(HttpMethod.GET, "/halls/{id}"),
                    paths.matcher(HttpMethod.GET, "/halls/{id}/seats")
                ).permitAll()
                .anyRequest().denyAll())
            .build();
    }
}


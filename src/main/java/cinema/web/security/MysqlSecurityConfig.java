package cinema.web.security;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import cinema.application.common.exceptions.ForbiddenException;
import cinema.application.common.interfaces.TokenVerifier;

@Configuration
@Profile("mysql")
public class MysqlSecurityConfig {

    @Bean
    SecurityFilterChain mysqlSecurityFilterChain(HttpSecurity http, TokenVerifier verifier,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver errors) throws Exception {
        return http
                // Header-only Bearer authentication; no browser cookie/session credentials.
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable()).httpBasic(basic -> basic.disable())
                .logout(logout -> logout.disable()).requestCache(cache -> cache.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, ex) -> errors.resolveException(
                                request, response, null, new ForbiddenException("Authentication required")))
                        .accessDeniedHandler((request, response, ex) -> errors.resolveException(
                                request, response, null, new ForbiddenException("Access denied"))))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/identity/register", "/identity/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/cinemas", "/cinemas/{id}", "/cinemas/{id}/halls",
                                "/halls/{id}", "/halls/{id}/seats", "/movies", "/movies/{id}",
                                "/screenings", "/screenings/{id}", "/screenings/{id}/available-seats").permitAll()
                        .requestMatchers(HttpMethod.POST, "/cinemas", "/halls", "/seats", "/movies",
                                "/screenings", "/screenings/{id}/cancel").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/movies/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/identity/me", "/carts/me", "/bookings/me",
                                "/bookings/{id}", "/tickets/me", "/tickets/{id}").hasAnyRole("CUSTOMER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/carts/seats", "/bookings/checkout",
                                "/bookings/{id}/cancel", "/payments/{bookingId}").hasAnyRole("CUSTOMER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/carts", "/carts/seats").hasAnyRole("CUSTOMER", "ADMIN")
                        .anyRequest().denyAll())
                .addFilterBefore(new BearerAuthenticationFilter(verifier, errors), UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}

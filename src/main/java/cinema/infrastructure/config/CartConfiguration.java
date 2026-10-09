package cinema.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import cinema.application.common.interfaces.CartRepository;
import cinema.application.common.interfaces.SeatHoldService;
import cinema.application.common.interfaces.SeatReservationRepository;
import cinema.infrastructure.persistence.inmemory.InMemoryCartRepository;
import cinema.infrastructure.persistence.inmemory.InMemorySeatState;
import cinema.infrastructure.persistence.inmemory.InMemorySeatReservationRepository;
import cinema.infrastructure.persistence.inmemory.SimpleSeatHoldService;

@Configuration
@Profile("venue-dev & !mysql")
public class CartConfiguration {

    @Bean
    public CartRepository cartRepository() {
        return new InMemoryCartRepository();
    }

    @Bean
    public InMemorySeatState seatState() {
        return new InMemorySeatState();
    }

    @Bean
    public SeatHoldService seatHoldService(InMemorySeatState state) {
        return new SimpleSeatHoldService(state);
    }

    @Bean
    public SeatReservationRepository seatReservationRepository(InMemorySeatState state) {
        return new InMemorySeatReservationRepository(state);
    }
}

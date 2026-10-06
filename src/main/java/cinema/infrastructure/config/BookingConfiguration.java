package cinema.infrastructure.config;

import cinema.application.common.interfaces.BookingRepository;
import cinema.application.common.interfaces.Clock;
import cinema.infrastructure.persistence.inmemory.InMemoryBookingRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("venue-dev")
public class BookingConfiguration {
    @Bean
    public BookingRepository bookingRepository() {
        return new InMemoryBookingRepository();
    }

    @Bean
    public Clock clock() {
        return new SystemClock();
    }
}

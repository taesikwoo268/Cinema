package cinema.infrastructure.config;

import cinema.application.common.interfaces.*;
import cinema.infrastructure.persistence.inmemory.InMemoryTicketRepository;
import org.springframework.context.annotation.*;

@Configuration
@Profile("venue-dev")
public class TicketConfiguration {
    @Bean public TicketRepository ticketRepository(BookingRepository bookings) {
        return new InMemoryTicketRepository(bookings);
    }
}

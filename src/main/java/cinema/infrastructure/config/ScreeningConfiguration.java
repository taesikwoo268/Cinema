package cinema.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import cinema.application.common.interfaces.ScreeningRepository;
import cinema.infrastructure.persistence.inmemory.InMemoryScreeningRepository;

@Configuration
@Profile("venue-dev & !mysql")
public class ScreeningConfiguration {

    @Bean
    public ScreeningRepository screeningRepository() {
        return new InMemoryScreeningRepository();
    }
}

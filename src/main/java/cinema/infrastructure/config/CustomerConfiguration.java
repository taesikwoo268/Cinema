package cinema.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import cinema.application.common.interfaces.CustomerRepository;
import cinema.infrastructure.persistence.inmemory.InMemoryCustomerRepository;

@Configuration
@Profile("venue-dev & !mysql")
public class CustomerConfiguration {

    @Bean
    public CustomerRepository customerRepository() {
        return new InMemoryCustomerRepository();
    }
}
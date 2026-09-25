package cinema.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import cinema.application.common.interfaces.MovieRepository;
import cinema.infrastructure.persistence.inmemory.InMemoryMovieRepository;

@Configuration
@Profile("venue-dev")
public class MovieConfiguration {

    @Bean
    public MovieRepository movieRepository() {
        return new InMemoryMovieRepository();
    }
}

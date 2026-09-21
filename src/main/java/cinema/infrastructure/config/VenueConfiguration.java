package cinema.infrastructure.config;

import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import cinema.application.common.interfaces.CinemaRepository;
import cinema.application.common.interfaces.HallRepository;
import cinema.application.common.interfaces.SeatRepository;
import cinema.application.venues.commands.CreateCinemaCommandHandler;
import cinema.application.venues.commands.CreateHallCommandHandler;
import cinema.application.venues.commands.CreateSeatCommandHandler;
import cinema.application.venues.queries.GetCinemaQueryHandler;
import cinema.application.venues.queries.GetCinemasQueryHandler;
import cinema.application.venues.queries.GetHallQueryHandler;
import cinema.application.venues.queries.GetHallsQueryHandler;
import cinema.application.venues.queries.GetSeatsQueryHandler;
import cinema.infrastructure.persistence.inmemory.InMemoryCinemaRepository;
import cinema.infrastructure.persistence.inmemory.InMemoryHallRepository;
import cinema.infrastructure.persistence.inmemory.InMemorySeatRepository;

@Configuration
@Profile("venue-dev")
public class VenueConfiguration {

    @Bean
    public CinemaRepository cinemaRepository() {
        return new InMemoryCinemaRepository();
    }

    @Bean
    public HallRepository hallRepository() {
        return new InMemoryHallRepository();
    }

    @Bean
    public SeatRepository seatRepository() {
        return new InMemorySeatRepository();
    }

    @Bean
    public CreateCinemaCommandHandler createCinemaCommandHandler(CinemaRepository repository) {
        return new CreateCinemaCommandHandler(repository, () -> UUID.randomUUID().toString());
    }

    @Bean
    public CreateHallCommandHandler createHallCommandHandler(CinemaRepository cinemas, HallRepository halls) {
        return new CreateHallCommandHandler(cinemas, halls, () -> UUID.randomUUID().toString());
    }

    @Bean
    public CreateSeatCommandHandler createSeatCommandHandler(HallRepository halls, SeatRepository seats) {
        return new CreateSeatCommandHandler(halls, seats, () -> UUID.randomUUID().toString());
    }

    @Bean
    public GetCinemasQueryHandler getCinemasQueryHandler(CinemaRepository cinemas) {
        return new GetCinemasQueryHandler(cinemas);
    }

    @Bean
    public GetCinemaQueryHandler getCinemaQueryHandler(CinemaRepository cinemas) {
        return new GetCinemaQueryHandler(cinemas);
    }

    @Bean
    public GetHallsQueryHandler getHallsQueryHandler(CinemaRepository cinemas, HallRepository halls) {
        return new GetHallsQueryHandler(cinemas, halls);
    }

    @Bean
    public GetHallQueryHandler getHallQueryHandler(HallRepository halls) {
        return new GetHallQueryHandler(halls);
    }

    @Bean
    public GetSeatsQueryHandler getSeatsQueryHandler(HallRepository halls, SeatRepository seats) {
        return new GetSeatsQueryHandler(halls, seats);
    }
}

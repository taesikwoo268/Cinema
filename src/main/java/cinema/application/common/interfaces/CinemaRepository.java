package cinema.application.common.interfaces;

import java.util.List;
import java.util.Optional;

import cinema.domain.cinema.Cinema;

public interface CinemaRepository {

    Optional<Cinema> findById(String id);

    List<Cinema> findAll();

    void save(Cinema cinema);
}
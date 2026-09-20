package cinema.application.common.interfaces;

import java.util.List;
import java.util.Optional;

import cinema.domain.cinema.Hall;

public interface HallRepository {
    
    Optional<Hall> findById(String id);

    List<Hall> findByCinemaId(String cinemaId);

    boolean existsByCinemaIdAndName(String cinemaId, String name);
    
    void save(Hall hall);
}
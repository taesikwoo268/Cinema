package cinema.infrastructure.persistence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import cinema.infrastructure.persistence.entities.MovieJpaEntity;
public interface SpringDataMovieRepository extends JpaRepository<MovieJpaEntity, String> { }

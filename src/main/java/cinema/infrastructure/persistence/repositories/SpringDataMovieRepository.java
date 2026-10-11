package cinema.infrastructure.persistence.repositories;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import cinema.infrastructure.persistence.entities.MovieJpaEntity;

public interface SpringDataMovieRepository extends JpaRepository<MovieJpaEntity, String> {

    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from MovieJpaEntity e where e.id = :id")
    java.util.Optional<MovieJpaEntity> lockById(@Param("id") String id);
}

package cinema.infrastructure.persistence.repositories;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import cinema.infrastructure.persistence.entities.CinemaJpaEntity;

public interface SpringDataCinemaRepository extends JpaRepository<CinemaJpaEntity, String> {

    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from CinemaJpaEntity e where e.id = :id")
    java.util.Optional<CinemaJpaEntity> lockById(@Param("id") String id);
}

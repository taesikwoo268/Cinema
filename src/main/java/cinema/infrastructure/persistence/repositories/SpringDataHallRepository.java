package cinema.infrastructure.persistence.repositories;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import cinema.infrastructure.persistence.entities.HallJpaEntity;

public interface SpringDataHallRepository extends JpaRepository<HallJpaEntity, String> {
    java.util.List<HallJpaEntity> findByCinemaIdOrderByNameAscIdAsc(String id);
    boolean existsByCinemaIdAndName(String id, String name);
    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from HallJpaEntity e where e.id = :id")
    java.util.Optional<HallJpaEntity> lockById(@Param("id") String id);
}

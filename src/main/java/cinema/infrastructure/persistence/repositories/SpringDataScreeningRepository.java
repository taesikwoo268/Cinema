package cinema.infrastructure.persistence.repositories;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import cinema.infrastructure.persistence.entities.ScreeningJpaEntity;

public interface SpringDataScreeningRepository extends JpaRepository<ScreeningJpaEntity, String> {
    java.util.List<ScreeningJpaEntity> findByMovieIdOrderByIdAsc(String id);
    @Query("select count(s) > 0 from ScreeningJpaEntity s where s.hallId = :hall and s.startTime < :end and s.endTime > :start")
    boolean hasOverlap(@Param("hall") String hall, @Param("start") java.time.Instant start, @Param("end") java.time.Instant end);
    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from ScreeningJpaEntity e where e.id = :id")
    java.util.Optional<ScreeningJpaEntity> lockById(@Param("id") String id);
}

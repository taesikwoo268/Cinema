package cinema.infrastructure.persistence.repositories;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import cinema.infrastructure.persistence.entities.SeatJpaEntity;

public interface SpringDataSeatRepository extends JpaRepository<SeatJpaEntity, String> {
    java.util.List<SeatJpaEntity> findByHallIdOrderByRowAscNumberAscIdAsc(String id);
    boolean existsByHallIdAndRowAndNumber(String hallId, String row, int number);
    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from SeatJpaEntity e where e.id = :id")
    java.util.Optional<SeatJpaEntity> lockById(@Param("id") String id);
}

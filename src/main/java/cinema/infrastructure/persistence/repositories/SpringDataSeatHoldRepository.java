package cinema.infrastructure.persistence.repositories;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import cinema.infrastructure.persistence.entities.SeatHoldJpaEntity;

public interface SpringDataSeatHoldRepository extends JpaRepository<SeatHoldJpaEntity, String> {
    java.util.Optional<SeatHoldJpaEntity> findByScreeningIdAndSeatId(String screeningId, String seatId);
    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from SeatHoldJpaEntity e where e.id = :id")
    java.util.Optional<SeatHoldJpaEntity> lockById(@Param("id") String id);
}

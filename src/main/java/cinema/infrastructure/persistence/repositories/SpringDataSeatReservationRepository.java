package cinema.infrastructure.persistence.repositories;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import cinema.infrastructure.persistence.entities.SeatReservationJpaEntity;

public interface SpringDataSeatReservationRepository extends JpaRepository<SeatReservationJpaEntity, String> {
    java.util.Optional<SeatReservationJpaEntity> findByScreeningIdAndSeatId(String screeningId, String seatId);
    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from SeatReservationJpaEntity e where e.id = :id")
    java.util.Optional<SeatReservationJpaEntity> lockById(@Param("id") String id);
}

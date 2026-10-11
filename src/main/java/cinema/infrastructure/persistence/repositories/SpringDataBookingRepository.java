package cinema.infrastructure.persistence.repositories;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import cinema.infrastructure.persistence.entities.BookingJpaEntity;

public interface SpringDataBookingRepository extends JpaRepository<BookingJpaEntity, String> {
    java.util.List<BookingJpaEntity> findByCustomerIdOrderByCreatedAtAscIdAsc(String id);
    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from BookingJpaEntity e where e.id = :id")
    java.util.Optional<BookingJpaEntity> lockById(@Param("id") String id);
}

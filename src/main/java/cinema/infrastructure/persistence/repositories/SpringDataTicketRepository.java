package cinema.infrastructure.persistence.repositories;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import cinema.infrastructure.persistence.entities.TicketJpaEntity;

public interface SpringDataTicketRepository extends JpaRepository<TicketJpaEntity, String> {
    @Query("select t from TicketJpaEntity t join BookingJpaEntity b on b.id = t.bookingId where b.customerId = :customer order by t.id")
    java.util.List<TicketJpaEntity> findByCustomerId(@Param("customer") String customer);
    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from TicketJpaEntity e where e.id = :id")
    java.util.Optional<TicketJpaEntity> lockById(@Param("id") String id);
}

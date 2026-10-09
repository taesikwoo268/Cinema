package cinema.infrastructure.persistence.repositories;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import cinema.infrastructure.persistence.entities.PaymentJpaEntity;

public interface SpringDataPaymentRepository extends JpaRepository<PaymentJpaEntity, String> {
    java.util.List<PaymentJpaEntity> findByBookingIdOrderByIdAsc(String id);
    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from PaymentJpaEntity e where e.id = :id")
    java.util.Optional<PaymentJpaEntity> lockById(@Param("id") String id);
}

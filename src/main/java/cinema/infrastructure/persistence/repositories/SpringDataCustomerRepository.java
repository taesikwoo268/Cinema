package cinema.infrastructure.persistence.repositories;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import cinema.infrastructure.persistence.entities.CustomerJpaEntity;

public interface SpringDataCustomerRepository extends JpaRepository<CustomerJpaEntity, String> {
    // Command transactions acquire this first, before cart reads (including initial creation).
    // Keep the customer lock until commit so separately mapped carts share one database guard.
    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    java.util.Optional<CustomerJpaEntity> findByUserId(String userId);
    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from CustomerJpaEntity e where e.id = :id")
    java.util.Optional<CustomerJpaEntity> lockById(@Param("id") String id);
}

package cinema.infrastructure.persistence.repositories;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import cinema.infrastructure.persistence.entities.CartJpaEntity;

public interface SpringDataCartRepository extends JpaRepository<CartJpaEntity, String> {
    java.util.Optional<CartJpaEntity> findByCustomerId(String id);
    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from CartJpaEntity e where e.id = :id")
    java.util.Optional<CartJpaEntity> lockById(@Param("id") String id);
}

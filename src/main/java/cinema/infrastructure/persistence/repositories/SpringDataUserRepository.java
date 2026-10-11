package cinema.infrastructure.persistence.repositories;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import cinema.infrastructure.persistence.entities.UserJpaEntity;

public interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, String> {
    java.util.Optional<UserJpaEntity> findByEmail(String email);
    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from UserJpaEntity e where e.id = :id")
    java.util.Optional<UserJpaEntity> lockById(@Param("id") String id);
}

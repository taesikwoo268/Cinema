package cinema.infrastructure.persistence.repositories;

import cinema.infrastructure.persistence.entities.AccessTokenJpaEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataAccessTokenRepository extends JpaRepository<AccessTokenJpaEntity, String> {
    @EntityGraph(attributePaths = "user")
    Optional<AccessTokenJpaEntity> findByTokenHash(String tokenHash);
}

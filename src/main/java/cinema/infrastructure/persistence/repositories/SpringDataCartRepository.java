package cinema.infrastructure.persistence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import cinema.infrastructure.persistence.entities.CartJpaEntity;
public interface SpringDataCartRepository extends JpaRepository<CartJpaEntity, String> { }

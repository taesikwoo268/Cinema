package cinema.infrastructure.persistence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import cinema.infrastructure.persistence.entities.ScreeningJpaEntity;
public interface SpringDataScreeningRepository extends JpaRepository<ScreeningJpaEntity, String> { }

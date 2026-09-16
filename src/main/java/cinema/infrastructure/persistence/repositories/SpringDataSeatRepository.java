package cinema.infrastructure.persistence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import cinema.infrastructure.persistence.entities.SeatJpaEntity;
public interface SpringDataSeatRepository extends JpaRepository<SeatJpaEntity, String> { }

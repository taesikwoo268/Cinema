package cinema.infrastructure.persistence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import cinema.infrastructure.persistence.entities.TicketJpaEntity;
public interface SpringDataTicketRepository extends JpaRepository<TicketJpaEntity, String> { }

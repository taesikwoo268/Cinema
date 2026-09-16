package cinema.infrastructure.persistence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import cinema.infrastructure.persistence.entities.CustomerJpaEntity;
public interface SpringDataCustomerRepository extends JpaRepository<CustomerJpaEntity, String> { }

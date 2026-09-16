package cinema.infrastructure.persistence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import cinema.infrastructure.persistence.entities.PaymentJpaEntity;
public interface SpringDataPaymentRepository extends JpaRepository<PaymentJpaEntity, String> { }

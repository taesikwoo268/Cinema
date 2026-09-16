package cinema.infrastructure.persistence.mappers;

import cinema.domain.payment.Payment;
import cinema.infrastructure.persistence.entities.PaymentJpaEntity;
public interface PaymentPersistenceMapper {
    Payment toDomain(PaymentJpaEntity entity);
    PaymentJpaEntity toEntity(Payment model);
}

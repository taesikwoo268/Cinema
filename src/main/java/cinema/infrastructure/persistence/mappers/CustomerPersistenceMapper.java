package cinema.infrastructure.persistence.mappers;

import cinema.domain.customer.Customer;
import cinema.infrastructure.persistence.entities.CustomerJpaEntity;
public interface CustomerPersistenceMapper {
    Customer toDomain(CustomerJpaEntity entity);
    CustomerJpaEntity toEntity(Customer model);
}

package cinema.infrastructure.persistence.mappers;

import cinema.domain.customer.Customer;
import cinema.domain.common.Money;
import cinema.infrastructure.persistence.entities.*;
import java.util.Currency;

public final class CustomerPersistenceMapper {
    public Customer toDomain(CustomerJpaEntity e) {
        return new Customer(e.id, e.userId, e.displayName);
    }
    public CustomerJpaEntity toEntity(Customer model) {
        var e = new CustomerJpaEntity();
        e.id = model.getId();
        e.userId = model.getUserId();
        e.displayName = model.getDisplayName();
        return e;
    }
}

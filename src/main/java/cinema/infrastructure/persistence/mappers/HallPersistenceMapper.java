package cinema.infrastructure.persistence.mappers;

import cinema.domain.cinema.Hall;
import cinema.domain.common.Money;
import cinema.infrastructure.persistence.entities.*;
import java.util.Currency;

public final class HallPersistenceMapper {
    public Hall toDomain(HallJpaEntity e) {
        return new Hall(e.id, e.cinemaId, e.name);
    }
    public HallJpaEntity toEntity(Hall model) {
        var e = new HallJpaEntity();
        e.id = model.getId();
        e.cinemaId = model.getCinemaId();
        e.name = model.getName();
        return e;
    }
}

package cinema.infrastructure.persistence.mappers;

import cinema.domain.cinema.Cinema;
import cinema.domain.common.Money;
import cinema.infrastructure.persistence.entities.*;
import java.util.Currency;

public final class CinemaPersistenceMapper {
    public Cinema toDomain(CinemaJpaEntity e) {
        return new Cinema(e.id, e.name, e.address);
    }
    public CinemaJpaEntity toEntity(Cinema model) {
        var e = new CinemaJpaEntity();
        e.id = model.getId();
        e.name = model.getName();
        e.address = model.getAddress();
        return e;
    }
}

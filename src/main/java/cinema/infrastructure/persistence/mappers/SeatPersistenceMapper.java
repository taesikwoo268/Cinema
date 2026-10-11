package cinema.infrastructure.persistence.mappers;

import cinema.domain.cinema.Seat;
import cinema.domain.common.Money;
import cinema.infrastructure.persistence.entities.*;
import java.util.Currency;

public final class SeatPersistenceMapper {
    public Seat toDomain(SeatJpaEntity e) {
        return new Seat(e.id, e.hallId, e.row, e.number, e.type);
    }
    public SeatJpaEntity toEntity(Seat model) {
        var e = new SeatJpaEntity();
        e.id = model.getId();
        e.hallId = model.getHallId();
        e.row = model.getRow();
        e.number = model.getNumber();
        e.type = model.getType();
        return e;
    }
}

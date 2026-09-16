package cinema.infrastructure.persistence.mappers;

import cinema.domain.cinema.Seat;
import cinema.infrastructure.persistence.entities.SeatJpaEntity;
public interface SeatPersistenceMapper {
    Seat toDomain(SeatJpaEntity entity);
    SeatJpaEntity toEntity(Seat model);
}

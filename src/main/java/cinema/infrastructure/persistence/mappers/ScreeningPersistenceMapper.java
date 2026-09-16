package cinema.infrastructure.persistence.mappers;

import cinema.domain.screening.Screening;
import cinema.infrastructure.persistence.entities.ScreeningJpaEntity;
public interface ScreeningPersistenceMapper {
    Screening toDomain(ScreeningJpaEntity entity);
    ScreeningJpaEntity toEntity(Screening model);
}

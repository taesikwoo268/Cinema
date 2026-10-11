package cinema.infrastructure.persistence.mappers;

import cinema.domain.screening.Screening;
import cinema.domain.common.Money;
import cinema.infrastructure.persistence.entities.*;
import java.util.Currency;

public final class ScreeningPersistenceMapper {
    public Screening toDomain(ScreeningJpaEntity e) {
        return new Screening(e.id, e.movieId, e.hallId, e.startTime, e.endTime, new Money(e.amount, Currency.getInstance(e.currency)), e.status);
    }
    public ScreeningJpaEntity toEntity(Screening model) {
        var e = new ScreeningJpaEntity();
        e.id = model.getId();
        e.movieId = model.getMovieId();
        e.hallId = model.getHallId();
        e.startTime = model.getStartTime();
        e.endTime = model.getEndTime();
        e.amount = PersistenceMoney.amount(model.getBasePrice());
        e.currency = model.getBasePrice().currency().getCurrencyCode();
        e.status = model.getStatus();
        return e;
    }
}

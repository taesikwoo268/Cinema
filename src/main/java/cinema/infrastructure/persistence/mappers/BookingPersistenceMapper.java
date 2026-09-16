package cinema.infrastructure.persistence.mappers;

import cinema.domain.booking.Booking;
import cinema.infrastructure.persistence.entities.BookingJpaEntity;
public interface BookingPersistenceMapper {
    Booking toDomain(BookingJpaEntity entity);
    BookingJpaEntity toEntity(Booking model);
}

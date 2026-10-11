package cinema.infrastructure.persistence.mappers;

import cinema.domain.booking.Booking;
import cinema.domain.common.Money;
import cinema.infrastructure.persistence.entities.*;
import java.util.Currency;

public final class BookingPersistenceMapper {
    public Booking toDomain(BookingJpaEntity e) {
        return new Booking(e.id, e.customerId, e.screeningId, e.items.stream().map(i -> new cinema.domain.booking.BookingItem(i.seatId, new Money(i.amount, Currency.getInstance(i.currency)))).toList(), new Money(e.amount, Currency.getInstance(e.currency)), e.status, e.createdAt);
    }
    public BookingJpaEntity toEntity(Booking model) {
        var e = new BookingJpaEntity();
        e.id = model.getId();
        e.customerId = model.getCustomerId();
        e.screeningId = model.getScreeningId();
        e.amount = PersistenceMoney.amount(model.getTotalAmount());
        e.currency = model.getTotalAmount().currency().getCurrencyCode();
        e.status = model.getStatus();
        e.createdAt = model.getCreatedAt();
        for (var item : model.getItems()) {
            var value = new BookingItemJpaValue();
            value.seatId = item.seatId();
            value.amount = PersistenceMoney.amount(item.price());
            value.currency = item.price().currency().getCurrencyCode();
            e.items.add(value);
        }
        return e;
    }
}

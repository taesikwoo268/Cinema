package cinema.infrastructure.persistence.mappers;

import cinema.domain.payment.Payment;
import cinema.domain.common.Money;
import cinema.infrastructure.persistence.entities.*;
import java.util.Currency;

public final class PaymentPersistenceMapper {
    public Payment toDomain(PaymentJpaEntity e) {
        return new Payment(e.id, e.bookingId, new Money(e.amount, Currency.getInstance(e.currency)), e.status, e.providerReference);
    }
    public PaymentJpaEntity toEntity(Payment model) {
        var e = new PaymentJpaEntity();
        e.id = model.getId();
        e.bookingId = model.getBookingId();
        e.amount = PersistenceMoney.amount(model.getAmount());
        e.currency = model.getAmount().currency().getCurrencyCode();
        e.status = model.getStatus();
        e.providerReference = model.getProviderReference();
        e.successfulBookingId = model.getStatus() == cinema.domain.payment.PaymentStatus.SUCCESS ? model.getBookingId() : null;
        return e;
    }
}

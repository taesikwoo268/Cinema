package cinema.infrastructure.payment;

import cinema.application.common.interfaces.PaymentGateway;
import cinema.application.common.models.*;
import cinema.domain.common.Money;
/** Adapter skeleton; not registered as a working provider. */
public final class FakePaymentGateway implements PaymentGateway {
    @Override public PaymentResult pay(String bookingId, Money amount) { throw new UnsupportedOperationException("Architecture Contract v0.1: not implemented" ); }
    @Override public PaymentResult refund(String paymentReference, Money amount) { throw new UnsupportedOperationException("Architecture Contract v0.1: not implemented" ); }
}

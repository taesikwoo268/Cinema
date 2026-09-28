package cinema.infrastructure.payment;

import cinema.application.common.interfaces.PaymentGateway;
import cinema.application.common.models.PaymentResult;
import cinema.domain.common.Money;
import java.util.concurrent.atomic.AtomicLong;

/** In-memory fake provider for the M7 development slice. */
public final class FakePaymentGateway implements PaymentGateway {
    private final AtomicLong paymentSequence = new AtomicLong();

    @Override
    public PaymentResult pay(String bookingId, Money amount) {
        if (bookingId == null || bookingId.isBlank()) {
            throw new IllegalArgumentException("bookingId must not be blank");
        }
        if (amount == null) {
            throw new IllegalArgumentException("amount must not be null");
        }

        String reference = "FAKE-PAY-" + paymentSequence.incrementAndGet();
        return new PaymentResult(true, reference);
    }

    @Override public PaymentResult refund(String paymentReference, Money amount) { throw new UnsupportedOperationException("Architecture Contract v0.1: not implemented" ); }
}

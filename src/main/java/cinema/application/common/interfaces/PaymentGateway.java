package cinema.application.common.interfaces;

import cinema.application.common.models.PaymentResult;
import cinema.domain.common.Money;

public interface PaymentGateway {

    PaymentResult pay(String bookingId, Money amount);

    PaymentResult refund(String paymentReference, Money amount);
}

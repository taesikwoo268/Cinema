package cinema.domain.payment;

import cinema.domain.common.BaseEntity;
import cinema.domain.common.Money;
import java.time.Instant;
import java.util.List;

/** Domain state skeleton; invariants and transitions are intentionally pending. */
public final class Payment extends BaseEntity {
    private final String bookingId;
    private final Money amount;
    private final PaymentStatus status;
    private final String providerReference;
    public Payment(String id, String bookingId, Money amount, PaymentStatus status, String providerReference) {
        super(id);
        this.bookingId = bookingId;
        this.amount = amount;
        this.status = status;
        this.providerReference = providerReference;
    }
    public String getBookingId() { return bookingId; }
    public Money getAmount() { return amount; }
    public PaymentStatus getStatus() { return status; }
    public String getProviderReference() { return providerReference; }
}

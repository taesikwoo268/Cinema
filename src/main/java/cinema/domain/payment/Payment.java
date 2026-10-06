package cinema.domain.payment;

import cinema.domain.common.BaseEntity;
import cinema.domain.common.DomainException;
import cinema.domain.common.Money;

/**
 * Immutable snapshot of a single payment attempt.
 * The state is decided by the caller (after PaymentGateway returns) and cannot transition.
 */
public final class Payment extends BaseEntity {
    private final String bookingId;
    private final Money amount;
    private final PaymentStatus status;
    private final String providerReference;
    public Payment(String id, String bookingId, Money amount, PaymentStatus status, String providerReference) {
        super(id);
        if (bookingId == null || bookingId.isBlank()) {
            throw new DomainException("Booking ID must not be blank");
        }
        if (amount == null) {
            throw new DomainException("Payment amount must not be null");
        }
        if (status == null) {
            throw new DomainException("Payment status must not be null");
        }
        if (requiresProviderReference(status) && (providerReference == null || providerReference.isBlank())) {
            throw new DomainException("Provider reference must not be blank for " + status + " payment");
        }
        this.bookingId = bookingId;
        this.amount = amount;
        this.status = status;
        this.providerReference = providerReference;
    }

    private static boolean requiresProviderReference(PaymentStatus status) {
        return status == PaymentStatus.SUCCESS || status == PaymentStatus.REFUNDED;
    }

    public String getBookingId() { return bookingId; }
    public Money getAmount() { return amount; }
    public PaymentStatus getStatus() { return status; }
    public String getProviderReference() { return providerReference; }
}

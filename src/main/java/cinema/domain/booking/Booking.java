package cinema.domain.booking;

import cinema.domain.common.BaseEntity;
import cinema.domain.common.Money;
import cinema.domain.common.DomainException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** Booking snapshot with transitions from pending to confirmed or cancelled. */
public final class Booking extends BaseEntity {
    private final String customerId;
    private final String screeningId;
    private final List<BookingItem> items;
    private final Money totalAmount;
    private BookingStatus status;
    private final Instant createdAt;
    public Booking(String id, String customerId, String screeningId, List<BookingItem> items, Money totalAmount, BookingStatus status, Instant createdAt) {
        super(id);
        this.customerId = customerId;
        this.screeningId = screeningId;
        if (items == null || items.isEmpty()) {
            throw new DomainException("Booking must contain at least one item");
        }
        if (items.stream().anyMatch(item -> item == null)) {
            throw new DomainException("Booking items must not contain null");
        }
        this.items = List.copyOf(items);
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = createdAt;
    }
    public String getCustomerId() { return customerId; }
    public String getScreeningId() { return screeningId; }
    public List<BookingItem> getItems() { return items; }
    public Money getTotalAmount() { return totalAmount; }
    public BookingStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public void confirm() {
        if (status != BookingStatus.PENDING) {
            throw new DomainException("Only pending bookings can be confirmed");
        }
        status = BookingStatus.CONFIRMED;
    }
    public void cancel() {
        if (status != BookingStatus.PENDING) {
            throw new DomainException("Only pending bookings can be cancelled");
        }
        status = BookingStatus.CANCELLED;
    }
    public Money calculateTotal() {
        var currency = items.get(0).price().currency();
        BigDecimal amount = BigDecimal.ZERO;
        for (BookingItem item : items) {
            if (!currency.equals(item.price().currency())) {
                throw new DomainException("Booking item currencies must match");
            }
            amount = amount.add(item.price().amount());
        }
        return new Money(amount, currency);
    }
}

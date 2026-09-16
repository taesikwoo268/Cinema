package cinema.domain.booking;

import cinema.domain.common.BaseEntity;
import cinema.domain.common.Money;
import java.time.Instant;
import java.util.List;

/** Domain state skeleton; invariants and transitions are intentionally pending. */
public final class Booking extends BaseEntity {
    private final String customerId;
    private final String screeningId;
    private final List<BookingItem> items;
    private final Money totalAmount;
    private final BookingStatus status;
    private final Instant createdAt;
    public Booking(String id, String customerId, String screeningId, List<BookingItem> items, Money totalAmount, BookingStatus status, Instant createdAt) {
        super(id);
        this.customerId = customerId;
        this.screeningId = screeningId;
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
    public void confirm() { throw new UnsupportedOperationException("Architecture Contract v0.1: not implemented" ); }
    public void cancel() { throw new UnsupportedOperationException("Architecture Contract v0.1: not implemented" ); }
    public Money calculateTotal() { throw new UnsupportedOperationException("Architecture Contract v0.1: not implemented" ); }
}

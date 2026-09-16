package cinema.domain.cart;

import cinema.domain.common.BaseEntity;
import cinema.domain.common.Money;
import java.time.Instant;
import java.util.List;

/** Domain state skeleton; invariants and transitions are intentionally pending. */
public final class Cart extends BaseEntity {
    private final String customerId;
    private final List<CartItem> items;
    public Cart(String id, String customerId, List<CartItem> items) {
        super(id);
        this.customerId = customerId;
        this.items = List.copyOf(items);
    }
    public String getCustomerId() { return customerId; }
    public List<CartItem> getItems() { return items; }
    public void addItem(CartItem item) { throw new UnsupportedOperationException("Architecture Contract v0.1: not implemented" ); }
    public void removeItem(String screeningId, String seatId) { throw new UnsupportedOperationException("Architecture Contract v0.1: not implemented" ); }
    public void clear() { throw new UnsupportedOperationException("Architecture Contract v0.1: not implemented" ); }
    public Money calculateTotal() { throw new UnsupportedOperationException("Architecture Contract v0.1: not implemented" ); }
}

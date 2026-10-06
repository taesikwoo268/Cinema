package cinema.domain.cart;

import cinema.domain.common.BaseEntity;
import cinema.domain.common.DomainException;
import cinema.domain.common.Money;
import java.util.ArrayList;
import java.util.List;

/** Customer cart with unique screening/seat pairs. */
public final class Cart extends BaseEntity {
    private final String customerId;
    private final List<CartItem> items;

    public Cart(String id, String customerId, List<CartItem> items) {
        super(id);
        if (customerId == null || customerId.isBlank()) {
            throw new DomainException("Cart customerId must not be blank");
        }
        if (items == null) {
            throw new DomainException("Cart items must not be null");
        }
        this.customerId = customerId;
        this.items = new ArrayList<>();
        for (CartItem item : items) {
            addItem(item);
        }
    }

    public String getCustomerId() { return customerId; }
    public List<CartItem> getItems() { return List.copyOf(items); }

    public void addItem(CartItem item) {
        if (item == null) {
            throw new DomainException("Cart item must not be null");
        }
        if (items.stream().anyMatch(existing ->
                existing.screeningId().equals(item.screeningId())
                        && existing.seatId().equals(item.seatId()))) {
            throw new DomainException("Seat already in cart for screening");
        }
        items.add(item);
    }

    public void removeItem(String screeningId, String seatId) {
        if (screeningId == null || screeningId.isBlank()) {
            throw new DomainException("Cart screeningId must not be blank");
        }
        if (seatId == null || seatId.isBlank()) {
            throw new DomainException("Cart seatId must not be blank");
        }
        if (!items.removeIf(item -> item.screeningId().equals(screeningId)
                && item.seatId().equals(seatId))) {
            throw new DomainException("Cart item not found");
        }
    }

    public void clear() { items.clear(); }
    public Money calculateTotal() { throw new UnsupportedOperationException("Architecture Contract v0.1: not implemented" ); }
}

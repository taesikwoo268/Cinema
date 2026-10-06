package cinema.domain.cart;

import cinema.domain.common.DomainException;
import cinema.domain.common.Money;

public record CartItem(String screeningId, String seatId, Money unitPrice) {
    public CartItem {
        if (screeningId == null || screeningId.isBlank()) {
            throw new DomainException("CartItem screeningId must not be blank");
        }
        if (seatId == null || seatId.isBlank()) {
            throw new DomainException("CartItem seatId must not be blank");
        }
        if (unitPrice == null) {
            throw new DomainException("CartItem unitPrice must not be null");
        }
    }
}

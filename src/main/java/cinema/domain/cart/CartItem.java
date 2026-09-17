package cinema.domain.cart;

import cinema.domain.common.Money;

public record CartItem(String screeningId, String seatId, Money unitPrice) {

}

package cinema.infrastructure.persistence.mappers;

import cinema.domain.cart.Cart;
import cinema.domain.common.Money;
import cinema.infrastructure.persistence.entities.*;
import java.util.Currency;

public final class CartPersistenceMapper {
    public Cart toDomain(CartJpaEntity e) {
        return new Cart(e.id, e.customerId, e.items.stream().map(i -> new cinema.domain.cart.CartItem(i.screeningId, i.seatId, new Money(i.amount, Currency.getInstance(i.currency)))).toList());
    }
    public CartJpaEntity toEntity(Cart model) {
        var e = new CartJpaEntity();
        e.id = model.getId();
        e.customerId = model.getCustomerId();
        for (var item : model.getItems()) {
            var value = new CartItemJpaValue();
            value.screeningId = item.screeningId();
            value.seatId = item.seatId();
            value.amount = PersistenceMoney.amount(item.unitPrice());
            value.currency = item.unitPrice().currency().getCurrencyCode();
            e.items.add(value);
        }
        return e;
    }
}

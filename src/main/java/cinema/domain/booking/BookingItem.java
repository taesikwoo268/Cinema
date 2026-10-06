package cinema.domain.booking;

import cinema.domain.common.DomainException;
import cinema.domain.common.Money;

public record BookingItem(String seatId, Money price) {
    public BookingItem {
        if (seatId == null || seatId.isBlank()) {
            throw new DomainException("BookingItem seatId must not be blank");
        }
        if (price == null) {
            throw new DomainException("BookingItem price must not be null");
        }
    }
}

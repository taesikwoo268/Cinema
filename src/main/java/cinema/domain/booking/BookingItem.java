package cinema.domain.booking;

import cinema.domain.common.Money; public record BookingItem(String seatId, Money price) { }

package cinema.application.common.models;

import java.math.BigDecimal;

public record BookingView(String id, String screeningId, String status, BigDecimal totalAmount, String currency) {

}

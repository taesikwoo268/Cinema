package cinema.web.requests;

import java.math.BigDecimal;
import java.time.Instant;

public record CreateScreeningRequest(String movieId, String hallId, Instant startTime, Instant endTime,
        BigDecimal basePrice, String currency) {

}

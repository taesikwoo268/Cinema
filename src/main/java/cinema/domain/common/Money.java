package cinema.domain.common;

import java.math.BigDecimal;
import java.util.Currency;
/** A non-negative amount in an explicit currency; precision is preserved. */
public record Money(BigDecimal amount, Currency currency) {
    public Money {
        if (amount == null) {
            throw new DomainException("Money amount must not be null");
        }
        if (currency == null) {
            throw new DomainException("Money currency must not be null");
        }
        if (amount.signum() < 0) {
            throw new DomainException("Money amount must not be negative");
        }
    }
}

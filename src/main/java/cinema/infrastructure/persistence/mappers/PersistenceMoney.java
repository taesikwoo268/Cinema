package cinema.infrastructure.persistence.mappers;

import cinema.application.common.exceptions.ValidationException;
import cinema.domain.common.Money;
import java.math.BigDecimal;
import java.math.RoundingMode;

/** Reject unrepresentable amounts instead of allowing the database to round money silently. */
final class PersistenceMoney {
    private PersistenceMoney() { }
    static BigDecimal amount(Money money) {
        try {
            var amount = money.amount().setScale(12, RoundingMode.UNNECESSARY);
            if (amount.precision() > 38) throw new ArithmeticException();
            return amount;
        } catch (ArithmeticException ex) {
            throw new ValidationException("Amount exceeds database precision (26 integer and 12 fractional digits)");
        }
    }
}

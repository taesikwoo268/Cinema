package cinema.domain.common;

import java.math.BigDecimal;
import java.util.Currency;
/** Value shape only; non-negative and currency invariants are pending implementation. */
public record Money(BigDecimal amount, Currency currency) { }

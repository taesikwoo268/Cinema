package cinema.domain;

import cinema.domain.common.BaseEntity;
import cinema.domain.common.DomainException;
import cinema.domain.common.Money;
import java.math.BigDecimal;
import java.util.Currency;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** M0 foundation rules. Feature-specific acceptance criteria live in the roadmap. */
class DomainRulesTest {
    private static final Currency VND = Currency.getInstance("VND");

    @Test void moneyRejectsNullAmount() {
        assertThrows(DomainException.class, () -> new Money(null, VND));
    }

    @Test void moneyRejectsNullCurrency() {
        assertThrows(DomainException.class, () -> new Money(BigDecimal.ZERO, null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "-0.001"})
    void moneyRejectsNegativeAmounts(String amount) {
        assertThrows(DomainException.class, () -> new Money(new BigDecimal(amount), VND));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.00", "125000.50"})
    void moneyPreservesValidAmountAndCurrency(String value) {
        BigDecimal amount = new BigDecimal(value);
        Money money = new Money(amount, VND);
        assertEquals(amount, money.amount());
        assertEquals(VND, money.currency());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void entityRejectsMissingOrBlankId(String id) {
        assertThrows(DomainException.class, () -> new TestEntity(id));
    }

    @Test void entityPreservesValidId() {
        assertEquals("cinema-001", new TestEntity("cinema-001").getId());
    }

    private static final class TestEntity extends BaseEntity {
        private TestEntity(String id) { super(id); }
    }
}

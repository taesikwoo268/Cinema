package cinema.domain.customer;

import cinema.domain.common.BaseEntity;
import cinema.domain.common.Money;
import java.time.Instant;
import java.util.List;

/** Domain state skeleton; invariants and transitions are intentionally pending. */
public final class Customer extends BaseEntity {
    private final String userId;
    private final String displayName;
    public Customer(String id, String userId, String displayName) {
        super(id);
        this.userId = userId;
        this.displayName = displayName;
    }
    public String getUserId() { return userId; }
    public String getDisplayName() { return displayName; }
}

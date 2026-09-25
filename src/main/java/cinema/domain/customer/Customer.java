package cinema.domain.customer;

import cinema.domain.common.BaseEntity;
import cinema.domain.common.DomainException;

/**
 * Domain state skeleton; invariants and transitions are intentionally pending.
 */
public final class Customer extends BaseEntity {

    private final String userId;
    private final String displayName;

    public Customer(String id, String userId, String displayName) {
        super(id);

        if (userId == null || userId.isBlank()) {
            throw new DomainException("User ID must not be blank");
        }

        if (displayName == null || displayName.isBlank()) {
            throw new DomainException("Display name must not be blank");
        }

        this.userId = userId;
        this.displayName = displayName.trim();
    }

    public String getUserId() {
        return userId;
    }

    public String getDisplayName() {
        return displayName;
    }
}

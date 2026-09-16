package cinema.domain.cinema;

import cinema.domain.common.BaseEntity;
import cinema.domain.common.Money;
import java.time.Instant;
import java.util.List;

/** Domain state skeleton; invariants and transitions are intentionally pending. */
public final class Cinema extends BaseEntity {
    private final String name;
    private final String address;
    public Cinema(String id, String name, String address) {
        super(id);
        this.name = name;
        this.address = address;
    }
    public String getName() { return name; }
    public String getAddress() { return address; }
}

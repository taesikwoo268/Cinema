package cinema.domain.cinema;

import cinema.domain.common.BaseEntity;
import cinema.domain.common.Money;
import java.time.Instant;
import java.util.List;

/** Domain state skeleton; invariants and transitions are intentionally pending. */
public final class Hall extends BaseEntity {
    private final String cinemaId;
    private final String name;
    public Hall(String id, String cinemaId, String name) {
        super(id);
        this.cinemaId = cinemaId;
        this.name = name;
    }
    public String getCinemaId() { return cinemaId; }
    public String getName() { return name; }
}

package cinema.domain.cinema;

import cinema.domain.common.BaseEntity;
import cinema.domain.common.DomainException;

public final class Hall extends BaseEntity {
    private final String cinemaId;
    private final String name;
    public Hall(String id, String cinemaId, String name) {
        super(id);
        if (cinemaId == null || cinemaId.isBlank()) {
            throw new DomainException("Hall cinemaId must not be blank");
        }
        if (name == null || name.isBlank()) {
            throw new DomainException("Hall name must not be blank");
        }
        this.cinemaId = cinemaId;
        this.name = name.trim();
    }
    public String getCinemaId() { return cinemaId; }
    public String getName() { return name; }
}
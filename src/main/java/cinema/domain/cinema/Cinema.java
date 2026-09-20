package cinema.domain.cinema;

import cinema.domain.common.BaseEntity;
import cinema.domain.common.DomainException;

public final class Cinema extends BaseEntity {
    
    private final String name;
    private final String address;

    public Cinema(String id, String name, String address) {
        super(id);
        if (name == null || name.isBlank()) {
            throw new DomainException("Cinema name must not be blank");
        }
        if (address == null || address.isBlank()) {
            throw new DomainException("Cinema address must not be blank");
        }
        this.name = name.trim();
        this.address = address.trim();
    }
    public String getName() { return name; }
    public String getAddress() { return address; }
}

package cinema.domain.common;

/** Identity only. Persistence annotations belong to infrastructure. */
public abstract class BaseEntity {
    private final String id;
    protected BaseEntity(String id) {
        if (id == null || id.isBlank()) {
            throw new DomainException("Entity ID must not be blank");
        }
        this.id = id;
    }
    public final String getId() { return id; }
}

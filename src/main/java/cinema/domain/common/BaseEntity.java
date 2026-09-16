package cinema.domain.common;

/** Identity only. Persistence annotations belong to infrastructure. */
public abstract class BaseEntity {
    private final String id;
    protected BaseEntity(String id) { this.id = id; }
    public final String getId() { return id; }
}

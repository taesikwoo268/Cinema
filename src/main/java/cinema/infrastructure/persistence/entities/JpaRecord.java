package cinema.infrastructure.persistence.entities;

import jakarta.persistence.*;

@MappedSuperclass
public abstract class JpaRecord {
    @Id @Column(length = 128) public String id;
    public String getId() { return id; }
}

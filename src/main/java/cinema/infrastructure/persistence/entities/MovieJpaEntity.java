package cinema.infrastructure.persistence.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
/** Mapping skeleton: remaining columns and constraints are pending. */
@Entity
@Table(name = "movie_records")
public class MovieJpaEntity {
    @Id private String id;
    protected MovieJpaEntity() { }
    public String getId() { return id; }
}

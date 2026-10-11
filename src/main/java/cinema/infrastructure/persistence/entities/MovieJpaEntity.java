package cinema.infrastructure.persistence.entities;

import jakarta.persistence.*;
import cinema.domain.movie.MovieStatus;

@Entity
@Table(name = "movie_records")
public class MovieJpaEntity extends JpaRecord {
    @Column(nullable = false) public boolean deleted;
    @Column(name = "title", nullable = false, length = 255) public String title;
    @Column(name = "description", nullable = true, length = 4000) public String description;
    @Column(name = "duration_minutes", nullable = false) public int durationMinutes;
    @Column(name = "age_rating", nullable = true, length = 32) public String ageRating;
    @Enumerated(EnumType.STRING)
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.VARCHAR)
    @Column(name = "status", nullable = false, length = 32) public MovieStatus status;
}

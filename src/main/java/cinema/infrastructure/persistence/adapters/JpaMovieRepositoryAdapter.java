package cinema.infrastructure.persistence.adapters;

import cinema.application.common.interfaces.MovieRepository;
import cinema.application.common.exceptions.ConflictException;
import cinema.domain.movie.Movie;
import cinema.infrastructure.persistence.entities.*;
import cinema.infrastructure.persistence.repositories.*;
import cinema.infrastructure.persistence.mappers.MoviePersistenceMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Repository
@Profile("mysql")
@Transactional
public class JpaMovieRepositoryAdapter implements MovieRepository {
    private final SpringDataMovieRepository data;
    private final MoviePersistenceMapper mapper = new MoviePersistenceMapper();
    @PersistenceContext private EntityManager em;
    public JpaMovieRepositoryAdapter(SpringDataMovieRepository data) {
        this.data = data;
    }
    @Override public Optional<Movie> findById(String id) {
        return data.lockById(id).filter(e -> !e.deleted).map(mapper::toDomain);
    }
    @Override public List<Movie> findAll() {
        return data.findAll(org.springframework.data.domain.Sort.by("id")).stream().filter(e -> !e.deleted).map(mapper::toDomain).toList();
    }
    @Override public void deleteById(String id) {
        data.lockById(id).ifPresent(e -> e.deleted = true);
        data.flush();
    }
    @Override public void save(Movie model) {
        Objects.requireNonNull(model);
        data.lockById(model.getId());
        em.merge(mapper.toEntity(model));
        em.flush();
    }
}

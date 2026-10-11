package cinema.infrastructure.persistence.adapters;

import cinema.application.common.interfaces.CinemaRepository;
import cinema.application.common.exceptions.ConflictException;
import cinema.domain.cinema.Cinema;
import cinema.infrastructure.persistence.entities.*;
import cinema.infrastructure.persistence.repositories.*;
import cinema.infrastructure.persistence.mappers.CinemaPersistenceMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Repository
@Profile("mysql")
@Transactional
public class JpaCinemaRepositoryAdapter implements CinemaRepository {
    private final SpringDataCinemaRepository data;
    private final CinemaPersistenceMapper mapper = new CinemaPersistenceMapper();
    @PersistenceContext private EntityManager em;
    public JpaCinemaRepositoryAdapter(SpringDataCinemaRepository data) {
        this.data = data;
    }
    @Override public Optional<Cinema> findById(String id) {
        return data.lockById(id).map(mapper::toDomain);
    }
    @Override public List<Cinema> findAll() {
        return data.findAll(org.springframework.data.domain.Sort.by("id")).stream().map(mapper::toDomain).toList();
    }
    @Override public void save(Cinema model) {
        Objects.requireNonNull(model);
        if (data.existsById(model.getId())) throw new ConflictException("Cinema ID already exists");
        em.persist(mapper.toEntity(model));
        em.flush();
    }
}

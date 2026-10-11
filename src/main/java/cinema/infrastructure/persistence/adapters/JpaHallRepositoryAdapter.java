package cinema.infrastructure.persistence.adapters;

import cinema.application.common.interfaces.HallRepository;
import cinema.application.common.exceptions.ConflictException;
import cinema.domain.cinema.Hall;
import cinema.infrastructure.persistence.entities.*;
import cinema.infrastructure.persistence.repositories.*;
import cinema.infrastructure.persistence.mappers.HallPersistenceMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Repository
@Profile("mysql")
@Transactional
public class JpaHallRepositoryAdapter implements HallRepository {
    private final SpringDataHallRepository data;
    private final HallPersistenceMapper mapper = new HallPersistenceMapper();
    @PersistenceContext private EntityManager em;
    public JpaHallRepositoryAdapter(SpringDataHallRepository data) {
        this.data = data;
    }
    @Override public Optional<Hall> findById(String id) {
        return data.lockById(id).map(mapper::toDomain);
    }
    @Override public List<Hall> findByCinemaId(String cinemaId) {
        return data.findByCinemaIdOrderByNameAscIdAsc(cinemaId).stream().map(mapper::toDomain).toList();
    }
    @Override public boolean existsByCinemaIdAndName(String id, String name) { return data.existsByCinemaIdAndName(id, name); }
    @Override public void save(Hall model) {
        Objects.requireNonNull(model);
        if (data.existsById(model.getId())) throw new ConflictException("Hall ID already exists");
        em.persist(mapper.toEntity(model));
        em.flush();
    }
}

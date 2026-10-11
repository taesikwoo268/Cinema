package cinema.infrastructure.persistence.adapters;

import cinema.application.common.interfaces.ScreeningRepository;
import cinema.application.common.exceptions.ConflictException;
import cinema.domain.screening.Screening;
import cinema.infrastructure.persistence.entities.*;
import cinema.infrastructure.persistence.repositories.*;
import cinema.infrastructure.persistence.mappers.ScreeningPersistenceMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Repository
@Profile("mysql")
@Transactional
public class JpaScreeningRepositoryAdapter implements ScreeningRepository {
    private final SpringDataScreeningRepository data;
    private final ScreeningPersistenceMapper mapper = new ScreeningPersistenceMapper();
    @PersistenceContext private EntityManager em;
    private final SpringDataHallRepository halls;
    public JpaScreeningRepositoryAdapter(SpringDataScreeningRepository data, SpringDataHallRepository halls) {
        this.data = data;
        this.halls = halls;
    }
    @Override public Optional<Screening> findById(String id) {
        return data.lockById(id).map(mapper::toDomain);
    }
    @Override public List<Screening> findByMovieId(String movieId) {
        return data.findByMovieIdOrderByIdAsc(movieId).stream().map(mapper::toDomain).toList();
    }
    @Override public boolean hasOverlap(String hallId, java.time.Instant start, java.time.Instant end) {
        halls.lockById(hallId);
        return data.hasOverlap(hallId, start, end);
    }
    @Override public void save(Screening model) {
        Objects.requireNonNull(model);
        halls.lockById(model.getHallId());
        if (!data.existsById(model.getId()) && data.hasOverlap(model.getHallId(), model.getStartTime(), model.getEndTime())) {
            throw new ConflictException("Screening overlaps an existing screening");
        }
        data.lockById(model.getId());
        em.merge(mapper.toEntity(model));
        em.flush();
    }
}

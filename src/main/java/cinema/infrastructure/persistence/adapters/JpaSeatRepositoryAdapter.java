package cinema.infrastructure.persistence.adapters;

import cinema.application.common.interfaces.SeatRepository;
import cinema.application.common.exceptions.ConflictException;
import cinema.domain.cinema.Seat;
import cinema.infrastructure.persistence.entities.*;
import cinema.infrastructure.persistence.repositories.*;
import cinema.infrastructure.persistence.mappers.SeatPersistenceMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Repository
@Profile("mysql")
@Transactional
public class JpaSeatRepositoryAdapter implements SeatRepository {
    private final SpringDataSeatRepository data;
    private final SeatPersistenceMapper mapper = new SeatPersistenceMapper();
    @PersistenceContext private EntityManager em;
    public JpaSeatRepositoryAdapter(SpringDataSeatRepository data) {
        this.data = data;
    }
    @Override public Optional<Seat> findById(String id) {
        return data.lockById(id).map(mapper::toDomain);
    }
    @Override public List<Seat> findByHallId(String hallId) {
        return data.findByHallIdOrderByRowAscNumberAscIdAsc(hallId).stream().map(mapper::toDomain).toList();
    }
    @Override public boolean existsByHallIdAndRowAndNumber(String id, String row, int number) { return data.existsByHallIdAndRowAndNumber(id, row, number); }
    @Override public void save(Seat model) {
        Objects.requireNonNull(model);
        if (data.existsById(model.getId())) throw new ConflictException("Seat ID already exists");
        em.persist(mapper.toEntity(model));
        em.flush();
    }
}

package cinema.infrastructure.persistence.adapters;

import cinema.application.common.interfaces.BookingRepository;
import cinema.application.common.exceptions.ConflictException;
import cinema.domain.booking.Booking;
import cinema.infrastructure.persistence.entities.*;
import cinema.infrastructure.persistence.repositories.*;
import cinema.infrastructure.persistence.mappers.BookingPersistenceMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Repository
@Profile("mysql")
@Transactional
public class JpaBookingRepositoryAdapter implements BookingRepository {
    private final SpringDataBookingRepository data;
    private final BookingPersistenceMapper mapper = new BookingPersistenceMapper();
    @PersistenceContext private EntityManager em;
    public JpaBookingRepositoryAdapter(SpringDataBookingRepository data) {
        this.data = data;
    }
    @Override public Optional<Booking> findById(String id) {
        return data.lockById(id).map(mapper::toDomain);
    }
    @Override public List<Booking> findByCustomerId(String customerId) {
        return data.findByCustomerIdOrderByCreatedAtAscIdAsc(customerId).stream().map(mapper::toDomain).toList();
    }
    @Override public void save(Booking model) {
        Objects.requireNonNull(model);
        data.lockById(model.getId());
        em.merge(mapper.toEntity(model));
        em.flush();
    }
}

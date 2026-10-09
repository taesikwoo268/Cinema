package cinema.infrastructure.persistence.adapters;

import cinema.application.common.interfaces.PaymentRepository;
import cinema.application.common.exceptions.ConflictException;
import cinema.domain.payment.Payment;
import cinema.infrastructure.persistence.entities.*;
import cinema.infrastructure.persistence.repositories.*;
import cinema.infrastructure.persistence.mappers.PaymentPersistenceMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Repository
@Profile("mysql")
@Transactional
public class JpaPaymentRepositoryAdapter implements PaymentRepository {
    private final SpringDataPaymentRepository data;
    private final PaymentPersistenceMapper mapper = new PaymentPersistenceMapper();
    @PersistenceContext private EntityManager em;
    public JpaPaymentRepositoryAdapter(SpringDataPaymentRepository data) {
        this.data = data;
    }
    @Override public Optional<Payment> findById(String id) {
        return data.lockById(id).map(mapper::toDomain);
    }
    @Override public List<Payment> findByBookingId(String bookingId) {
        return data.findByBookingIdOrderByIdAsc(bookingId).stream().map(mapper::toDomain).toList();
    }
    @Override public void save(Payment model) {
        Objects.requireNonNull(model);
        data.lockById(model.getId());
        em.merge(mapper.toEntity(model));
        em.flush();
    }
}

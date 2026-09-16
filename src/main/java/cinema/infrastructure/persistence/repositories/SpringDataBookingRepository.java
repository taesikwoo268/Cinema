package cinema.infrastructure.persistence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import cinema.infrastructure.persistence.entities.BookingJpaEntity;
public interface SpringDataBookingRepository extends JpaRepository<BookingJpaEntity, String> { }

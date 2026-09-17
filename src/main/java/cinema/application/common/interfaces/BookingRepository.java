package cinema.application.common.interfaces;

import java.util.List;
import java.util.Optional;

import cinema.domain.booking.Booking;
public interface BookingRepository {
    Optional<Booking> findById(String id);
    List<Booking> findByCustomerId(String customerId);
    void save(Booking booking);
}




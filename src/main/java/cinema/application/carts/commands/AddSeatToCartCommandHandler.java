package cinema.application.carts.commands;

import java.util.List;
import java.util.function.Supplier;

import cinema.application.common.exceptions.ConflictException;
import cinema.application.common.exceptions.ForbiddenException;
import cinema.application.common.exceptions.NotFoundException;
import cinema.application.common.exceptions.ValidationException;
import cinema.application.common.interfaces.CartRepository;
import cinema.application.common.interfaces.CurrentUser;
import cinema.application.common.interfaces.CustomerRepository;
import cinema.application.common.interfaces.ScreeningRepository;
import cinema.application.common.interfaces.SeatHoldService;
import cinema.application.common.interfaces.SeatRepository;
import cinema.application.common.interfaces.SeatReservationRepository;
import cinema.application.common.interfaces.UseCase;
import cinema.domain.cart.Cart;
import cinema.domain.cart.CartItem;
import cinema.domain.screening.ScreeningStatus;

public class AddSeatToCartCommandHandler implements UseCase<AddSeatToCartCommand, Void> {

    private final CurrentUser currentUser;
    private final CustomerRepository customers;
    private final ScreeningRepository screenings;
    private final SeatRepository seats;
    private final SeatReservationRepository reservations;
    private final SeatHoldService holds;
    private final CartRepository carts;
    private final Supplier<String> idGenerator;

    public AddSeatToCartCommandHandler(CurrentUser currentUser, CustomerRepository customers,
            ScreeningRepository screenings, SeatRepository seats,
            SeatReservationRepository reservations, SeatHoldService holds,
            CartRepository carts, Supplier<String> idGenerator) {
        this.currentUser = currentUser;
        this.customers = customers;
        this.screenings = screenings;
        this.seats = seats;
        this.reservations = reservations;
        this.holds = holds;
        this.carts = carts;
        this.idGenerator = idGenerator;
    }

    @Override
    public Void handle(AddSeatToCartCommand input) {
        if (input == null) {
            throw new ValidationException("AddSeatToCartCommand must not be null");
        }
        String screeningId = input.screeningId();
        String seatId = input.seatId();
        if (screeningId == null || screeningId.isBlank()) {
            throw new ValidationException("Screening ID must not be blank");
        }
        if (seatId == null || seatId.isBlank()) {
            throw new ValidationException("Seat ID must not be blank");
        }
        if (!currentUser.isAuthenticated()) {
            throw new ForbiddenException("Authentication required");
        }
        String userId = currentUser.getUserId();
        if (userId == null || userId.isBlank()) {
            throw new ForbiddenException("Authenticated user ID must not be blank");
        }
        var customer = customers.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Customer not found for user: " + userId));
        var screening = screenings.findById(screeningId)
                .orElseThrow(() -> new NotFoundException("Screening not found: " + screeningId));
        if (screening.getStatus() != ScreeningStatus.SCHEDULED) {
            throw new ValidationException("Screening must be scheduled");
        }
        var seat = seats.findById(seatId)
                .orElseThrow(() -> new NotFoundException("Seat not found: " + seatId));
        if (!screening.getHallId().equals(seat.getHallId())) {
            throw new ValidationException("Seat must belong to screening hall");
        }
        if (reservations.isBooked(screeningId, seatId)) {
            throw new ConflictException("Seat already booked for screening");
        }
        String customerId = customer.getId();
        // V1 keeps one Customer instance per user; guard first-cart creation through save.
        synchronized (customer) {
            Cart cart = carts.findActiveByCustomerId(customerId)
                    .orElseGet(() -> new Cart(idGenerator.get(), customerId, List.of()));
            synchronized (cart) {
                if (cart.getItems().stream().anyMatch(item ->
                        item.screeningId().equals(screeningId)
                                && item.seatId().equals(seatId))) {
                    throw new ConflictException("Seat already in cart for screening");
                }

                boolean alreadyHeld = holds.isHeldBy(customerId, screeningId, seatId);

                if (!holds.hold(customerId, screeningId, seatId)) {
                    throw new ConflictException("Seat held or permanently booked");
                }

                boolean added = false;
                try {
                    cart.addItem(new CartItem(
                            screeningId, seatId, screening.getBasePrice()));
                    added = true;
                    carts.save(cart);
                    return null;
                } catch (RuntimeException | Error original) {
                    if (added) {
                        try {
                            cart.removeItem(screeningId, seatId);
                        } catch (RuntimeException | Error cleanup) {
                            if (cleanup != original) original.addSuppressed(cleanup);
                        }
                    }
                    if (!alreadyHeld) {
                        try {
                            holds.release(customerId, screeningId, seatId);
                        } catch (RuntimeException | Error cleanup) {
                            if (cleanup != original) original.addSuppressed(cleanup);
                        }
                    }
                    throw original;
                }
            }
        }
    }
}

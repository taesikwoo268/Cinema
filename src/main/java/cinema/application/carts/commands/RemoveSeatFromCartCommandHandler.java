package cinema.application.carts.commands;

import cinema.application.common.exceptions.ForbiddenException;
import cinema.application.common.exceptions.NotFoundException;
import cinema.application.common.exceptions.ValidationException;
import cinema.application.common.interfaces.CartRepository;
import cinema.application.common.interfaces.CurrentUser;
import cinema.application.common.interfaces.CustomerRepository;
import cinema.application.common.interfaces.SeatHoldService;
import cinema.application.common.interfaces.UseCase;

public class RemoveSeatFromCartCommandHandler implements UseCase<RemoveSeatFromCartCommand, Void> {

    private final CurrentUser currentUser;
    private final CustomerRepository customers;
    private final CartRepository carts;
    private final SeatHoldService holds;

    public RemoveSeatFromCartCommandHandler(CurrentUser currentUser, CustomerRepository customers,
            CartRepository carts, SeatHoldService holds) {
        this.currentUser = currentUser;
        this.customers = customers;
        this.carts = carts;
        this.holds = holds;
    }

    @Override
    public Void handle(RemoveSeatFromCartCommand input) {
        if (input == null) {
            throw new ValidationException("RemoveSeatFromCartCommand must not be null");
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
        String customerId = customer.getId();
        var cart = carts.findActiveByCustomerId(customerId)
                .orElseThrow(() -> new NotFoundException("Active cart not found for customer: " + customerId));
        synchronized (cart) {
            var removed = cart.getItems().stream()
                    .filter(item -> item.screeningId().equals(screeningId) && item.seatId().equals(seatId))
                    .findFirst().orElseThrow(() -> new NotFoundException("Cart item not found"));

            cart.removeItem(screeningId, seatId);
            boolean released = false;
            try {
                holds.release(customerId, screeningId, seatId);
                released = true;
                carts.save(cart);
                return null;
            } catch (RuntimeException | Error original) {
                try {
                    cart.addItem(removed);
                } catch (RuntimeException | Error cleanup) {
                    if (cleanup != original) {
                        original.addSuppressed(cleanup);
                    }
                }
                if (released) {
                    try {
                        if (!holds.hold(customerId, screeningId, seatId)) {
                            throw new IllegalStateException("Could not restore seat hold");
                        }
                    } catch (RuntimeException | Error cleanup) {
                        if (cleanup != original) {
                            original.addSuppressed(cleanup);
                        }
                    }
                }
                throw original;
            }
        }
    }
}

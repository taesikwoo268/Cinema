package cinema.application.carts.commands;

import cinema.application.common.exceptions.ForbiddenException;
import cinema.application.common.exceptions.NotFoundException;
import cinema.application.common.exceptions.ValidationException;
import cinema.application.common.interfaces.CartRepository;
import cinema.application.common.interfaces.CurrentUser;
import cinema.application.common.interfaces.CustomerRepository;
import cinema.application.common.interfaces.SeatHoldService;
import cinema.application.common.interfaces.UseCase;

public final class ClearCartCommandHandler implements UseCase<ClearCartCommand, Void> {

    private final CurrentUser currentUser;
    private final CustomerRepository customers;
    private final CartRepository carts;
    private final SeatHoldService holds;

    public ClearCartCommandHandler(CurrentUser currentUser, CustomerRepository customers,
            CartRepository carts, SeatHoldService holds) {
        this.currentUser = currentUser;
        this.customers = customers;
        this.carts = carts;
        this.holds = holds;
    }

    @Override
    public Void handle(ClearCartCommand input) {
        if (input == null) {
            throw new ValidationException("ClearCartCommand must not be null");
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
            var snapshot = cart.getItems();
            cart.clear();
            int releasedCount = 0;
            try {
                for (var item : snapshot) {
                    holds.release(customerId, item.screeningId(), item.seatId());
                    releasedCount++;
                }
                carts.save(cart);
                return null;
            } catch (RuntimeException | Error original) {
                for (var item : snapshot) {
                    try {
                        cart.addItem(item);
                    } catch (RuntimeException | Error cleanup) {
                        if (cleanup != original) {
                            original.addSuppressed(cleanup);
                        }
                    }
                }
                for (int i = 0; i < releasedCount; i++) {
                    var item = snapshot.get(i);
                    try {
                        if (!holds.hold(customerId, item.screeningId(), item.seatId())) {
                            throw new IllegalStateException("Could not restore seat hold: "
                                    + item.screeningId() + "/" + item.seatId());
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

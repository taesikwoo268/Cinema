package cinema.application.carts.queries;

import cinema.application.common.exceptions.ForbiddenException;
import cinema.application.common.exceptions.NotFoundException;
import cinema.application.common.exceptions.ValidationException;
import cinema.application.common.interfaces.CartRepository;
import cinema.application.common.interfaces.CurrentUser;
import cinema.application.common.interfaces.CustomerRepository;
import cinema.application.common.interfaces.UseCase;
import cinema.application.common.models.CartItemView;
import cinema.application.common.models.CartView;

public final class GetMyCartQueryHandler implements UseCase<GetMyCartQuery, CartView> {
    private final CurrentUser currentUser;
    private final CustomerRepository customers;
    private final CartRepository carts;

    public GetMyCartQueryHandler(CurrentUser currentUser, CustomerRepository customers,
            CartRepository carts) {
        this.currentUser = currentUser;
        this.customers = customers;
        this.carts = carts;
    }

    @Override
    public CartView handle(GetMyCartQuery input) {
        if (input == null) {
            throw new ValidationException("GetMyCartQuery must not be null");
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
        var cart = carts.findActiveByCustomerId(customer.getId())
                .orElseThrow(() -> new NotFoundException("Active cart not found for customer: " + customer.getId()));
        var items = cart.getItems().stream()
                .map(item -> new CartItemView(item.screeningId(), item.seatId(),
                        item.unitPrice().amount(), item.unitPrice().currency().getCurrencyCode()))
                .toList();
        return new CartView(cart.getId(), cart.getCustomerId(), items);
    }
}

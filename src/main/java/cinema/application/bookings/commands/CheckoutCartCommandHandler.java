package cinema.application.bookings.commands;

import cinema.application.common.exceptions.*;
import cinema.application.common.interfaces.*;
import cinema.application.common.models.CheckoutResult;
import cinema.domain.booking.Booking;
import cinema.domain.booking.BookingItem;
import cinema.domain.booking.BookingStatus;
import cinema.domain.common.DomainException;
import cinema.domain.common.Money;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Supplier;

public class CheckoutCartCommandHandler implements UseCase<CheckoutCartCommand, CheckoutResult> {
    private final CurrentUser currentUser;
    private final CustomerRepository customers;
    private final CartRepository carts;
    private final BookingRepository bookings;
    private final SeatHoldService holds;
    private final Supplier<String> idGenerator;
    private final Clock clock;

    public CheckoutCartCommandHandler(CurrentUser currentUser, CustomerRepository customers,
            CartRepository carts, BookingRepository bookings, SeatHoldService holds,
            Supplier<String> idGenerator, Clock clock) {
        this.currentUser = currentUser;
        this.customers = customers;
        this.carts = carts;
        this.bookings = bookings;
        this.holds = holds;
        this.idGenerator = idGenerator;
        this.clock = clock;
    }

    @Override
    public CheckoutResult handle(CheckoutCartCommand input) {
        if (input == null) throw new ValidationException("CheckoutCartCommand must not be null");
        if (!currentUser.isAuthenticated()) throw new ForbiddenException("Authentication required");
        String userId = currentUser.getUserId();
        if (userId == null || userId.isBlank()) {
            throw new ForbiddenException("Authenticated user ID must not be blank");
        }
        var customer = customers.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Customer not found for user: " + userId));
        String customerId = customer.getId();
        var cart = carts.findActiveByCustomerId(customerId)
                .orElseThrow(() -> new NotFoundException("Active cart not found for customer: " + customerId));
        // V1 repositories return the same active cart instance to competing requests.
        synchronized (cart) {
            var items = cart.getItems();
            if (items.isEmpty()) throw new ValidationException("Cart must not be empty");
            for (var item : items) {
                if (!holds.isHeldBy(customerId, item.screeningId(), item.seatId())) {
                    throw new ConflictException("Seat hold missing for customer");
                }
            }
            var groups = new LinkedHashMap<String, List<BookingItem>>();
            for (var item : items) {
                groups.computeIfAbsent(item.screeningId(), ignored -> new ArrayList<>())
                        .add(new BookingItem(item.seatId(), item.unitPrice()));
            }
            var pending = new ArrayList<Booking>();
            for (var group : groups.entrySet()) {
                var currency = group.getValue().get(0).price().currency();
                BigDecimal amount = BigDecimal.ZERO;
                for (var item : group.getValue()) {
                    if (!currency.equals(item.price().currency())) {
                        throw new DomainException("Booking item currencies must match");
                    }
                    amount = amount.add(item.price().amount());
                }
                pending.add(new Booking(idGenerator.get(), customerId, group.getKey(), group.getValue(),
                        new Money(amount, currency), BookingStatus.PENDING, clock.now()));
            }
            for (var booking : pending) bookings.save(booking);
            cart.clear();
            carts.save(cart);
            return new CheckoutResult(pending.stream().map(Booking::getId).toList());
        }
    }
}

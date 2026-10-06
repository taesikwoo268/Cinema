package cinema.web.controllers;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import cinema.application.carts.commands.AddSeatToCartCommand;
import cinema.application.carts.commands.AddSeatToCartCommandHandler;
import cinema.application.carts.commands.ClearCartCommand;
import cinema.application.carts.commands.ClearCartCommandHandler;
import cinema.application.carts.commands.RemoveSeatFromCartCommand;
import cinema.application.carts.commands.RemoveSeatFromCartCommandHandler;
import cinema.application.carts.queries.GetMyCartQuery;
import cinema.application.carts.queries.GetMyCartQueryHandler;
import cinema.application.common.models.CartView;
import cinema.web.requests.CartSeatRequest;

@RestController
@Profile("venue-dev")
@RequestMapping("/carts")
public final class CartController {
    private final AddSeatToCartCommandHandler addSeatToCartCommand;
    private final RemoveSeatFromCartCommandHandler removeSeatFromCartCommand;
    private final ClearCartCommandHandler clearCartCommand;
    private final GetMyCartQueryHandler getMyCartQuery;

    public CartController(AddSeatToCartCommandHandler addSeatToCartCommand, RemoveSeatFromCartCommandHandler removeSeatFromCartCommand, ClearCartCommandHandler clearCartCommand, GetMyCartQueryHandler getMyCartQuery) {
        this.addSeatToCartCommand = addSeatToCartCommand;
        this.removeSeatFromCartCommand = removeSeatFromCartCommand;
        this.clearCartCommand = clearCartCommand;
        this.getMyCartQuery = getMyCartQuery;
    }

    @PostMapping("/seats")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addSeat(@RequestBody CartSeatRequest request) {
        addSeatToCartCommand.handle(new AddSeatToCartCommand(request.screeningId(), request.seatId()));
    }

    @DeleteMapping("/seats")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeSeat(@RequestBody CartSeatRequest request) {
        removeSeatFromCartCommand.handle(new RemoveSeatFromCartCommand(request.screeningId(), request.seatId()));
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clear() {
        clearCartCommand.handle(new ClearCartCommand());
    }

    @GetMapping("/me")
    public CartView getMyCart() {
        return getMyCartQuery.handle(new GetMyCartQuery());
    }
}

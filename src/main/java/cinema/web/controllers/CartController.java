package cinema.web.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import cinema.application.carts.commands.AddSeatToCartCommandHandler;
import cinema.application.carts.commands.RemoveSeatFromCartCommandHandler;
import cinema.application.carts.commands.ClearCartCommandHandler;
import cinema.application.carts.queries.GetMyCartQueryHandler;
/** HTTP methods, request mapping and response mapping are pending implementation. */
@RestController
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
}

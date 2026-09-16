package cinema.web.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import cinema.application.payments.commands.PayBookingCommandHandler;
/** HTTP methods, request mapping and response mapping are pending implementation. */
@RestController
@RequestMapping("/payments")
public final class PaymentController {
    private final PayBookingCommandHandler payBookingCommand;
    public PaymentController(PayBookingCommandHandler payBookingCommand) {
        this.payBookingCommand = payBookingCommand;
    }
}

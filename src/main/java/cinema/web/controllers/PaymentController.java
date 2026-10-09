package cinema.web.controllers;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cinema.application.common.models.PaymentResult;
import cinema.application.payments.commands.PayBookingCommand;
import cinema.application.payments.commands.PayBookingCommandHandler;

@RestController
@Profile({"venue-dev", "mysql"})
@RequestMapping("/payments")
public final class PaymentController {

    private final PayBookingCommandHandler payBookingCommand;

    public PaymentController(PayBookingCommandHandler payBookingCommand) {
        this.payBookingCommand = payBookingCommand;
    }

    @PostMapping("/{bookingId}")
    public PaymentResult pay(@PathVariable String bookingId) {
        return payBookingCommand.handle(
                new PayBookingCommand(bookingId));
    }
}
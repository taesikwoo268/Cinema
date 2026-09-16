package cinema.application.payments.commands;

import cinema.application.common.interfaces.UseCase;
import cinema.application.common.models.*;
import cinema.application.common.exceptions.FeatureNotImplementedException;
import java.util.List;
/** TODO: implement orchestration through application ports; see docs/architecture-contract.md. */
public final class PayBookingCommandHandler implements UseCase<PayBookingCommand, PaymentResult> {
    @Override public PaymentResult handle(PayBookingCommand input) { throw new FeatureNotImplementedException("PayBookingCommand"); }
}

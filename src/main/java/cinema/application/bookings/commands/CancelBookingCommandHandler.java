package cinema.application.bookings.commands;

import cinema.application.common.interfaces.UseCase;
import cinema.application.common.models.*;
import cinema.application.common.exceptions.FeatureNotImplementedException;
import java.util.List;
/** TODO: implement orchestration through application ports; see docs/architecture-contract.md. */
public final class CancelBookingCommandHandler implements UseCase<CancelBookingCommand, Void> {
    @Override public Void handle(CancelBookingCommand input) { throw new FeatureNotImplementedException("CancelBookingCommand"); }
}

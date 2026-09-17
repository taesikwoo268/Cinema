package cinema.application.bookings.commands;

import cinema.application.common.exceptions.FeatureNotImplementedException;
import cinema.application.common.interfaces.UseCase;

/**
 * TODO: implement orchestration through application ports; see
 * docs/architecture-contract.md.
 */
public final class CancelBookingCommandHandler implements UseCase<CancelBookingCommand, Void> {

    @Override
    public Void handle(CancelBookingCommand input) {
        throw new FeatureNotImplementedException("CancelBookingCommand");
    }
}

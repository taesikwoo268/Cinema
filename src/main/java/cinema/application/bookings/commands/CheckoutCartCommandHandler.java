package cinema.application.bookings.commands;

import cinema.application.common.exceptions.FeatureNotImplementedException;
import cinema.application.common.interfaces.UseCase;
import cinema.application.common.models.CheckoutResult;

/**
 * TODO: implement orchestration through application ports; see
 * docs/architecture-contract.md.
 */
public final class CheckoutCartCommandHandler implements UseCase<CheckoutCartCommand, CheckoutResult> {

    @Override
    public CheckoutResult handle(CheckoutCartCommand input) {
        throw new FeatureNotImplementedException("CheckoutCartCommand");
    }
}

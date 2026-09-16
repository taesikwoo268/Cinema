package cinema.application.carts.commands;

import cinema.application.common.interfaces.UseCase;
import cinema.application.common.models.*;
import cinema.application.common.exceptions.FeatureNotImplementedException;
import java.util.List;
/** TODO: implement orchestration through application ports; see docs/architecture-contract.md. */
public final class AddSeatToCartCommandHandler implements UseCase<AddSeatToCartCommand, Void> {
    @Override public Void handle(AddSeatToCartCommand input) { throw new FeatureNotImplementedException("AddSeatToCartCommand"); }
}

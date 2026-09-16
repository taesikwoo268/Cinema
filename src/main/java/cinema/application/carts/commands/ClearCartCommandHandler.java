package cinema.application.carts.commands;

import cinema.application.common.interfaces.UseCase;
import cinema.application.common.models.*;
import cinema.application.common.exceptions.FeatureNotImplementedException;
import java.util.List;
/** TODO: implement orchestration through application ports; see docs/architecture-contract.md. */
public final class ClearCartCommandHandler implements UseCase<ClearCartCommand, Void> {
    @Override public Void handle(ClearCartCommand input) { throw new FeatureNotImplementedException("ClearCartCommand"); }
}

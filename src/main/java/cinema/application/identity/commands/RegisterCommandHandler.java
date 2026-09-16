package cinema.application.identity.commands;

import cinema.application.common.interfaces.UseCase;
import cinema.application.common.models.*;
import cinema.application.common.exceptions.FeatureNotImplementedException;
import java.util.List;
/** TODO: implement orchestration through application ports; see docs/architecture-contract.md. */
public final class RegisterCommandHandler implements UseCase<RegisterCommand, String> {
    @Override public String handle(RegisterCommand input) { throw new FeatureNotImplementedException("RegisterCommand"); }
}

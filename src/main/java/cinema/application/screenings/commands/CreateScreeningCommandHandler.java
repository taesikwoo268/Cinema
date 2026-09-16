package cinema.application.screenings.commands;

import cinema.application.common.interfaces.UseCase;
import cinema.application.common.models.*;
import cinema.application.common.exceptions.FeatureNotImplementedException;
import java.util.List;
/** TODO: implement orchestration through application ports; see docs/architecture-contract.md. */
public final class CreateScreeningCommandHandler implements UseCase<CreateScreeningCommand, String> {
    @Override public String handle(CreateScreeningCommand input) { throw new FeatureNotImplementedException("CreateScreeningCommand"); }
}

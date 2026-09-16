package cinema.application.identity.commands;

import cinema.application.common.interfaces.UseCase;
import cinema.application.common.models.*;
import cinema.application.common.exceptions.FeatureNotImplementedException;
import java.util.List;
/** TODO: implement orchestration through application ports; see docs/architecture-contract.md. */
public final class LoginCommandHandler implements UseCase<LoginCommand, AuthenticationResult> {
    @Override public AuthenticationResult handle(LoginCommand input) { throw new FeatureNotImplementedException("LoginCommand"); }
}

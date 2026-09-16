package cinema.application.identity.queries;

import cinema.application.common.interfaces.UseCase;
import cinema.application.common.models.*;
import cinema.application.common.exceptions.FeatureNotImplementedException;
import java.util.List;
/** TODO: implement orchestration through application ports; see docs/architecture-contract.md. */
public final class GetCurrentUserQueryHandler implements UseCase<GetCurrentUserQuery, CurrentUserView> {
    @Override public CurrentUserView handle(GetCurrentUserQuery input) { throw new FeatureNotImplementedException("GetCurrentUserQuery"); }
}

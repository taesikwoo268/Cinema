package cinema.application.screenings.queries;

import cinema.application.common.interfaces.UseCase;
import cinema.application.common.models.*;
import cinema.application.common.exceptions.FeatureNotImplementedException;
import java.util.List;
/** TODO: implement orchestration through application ports; see docs/architecture-contract.md. */
public final class GetScreeningQueryHandler implements UseCase<GetScreeningQuery, ScreeningView> {
    @Override public ScreeningView handle(GetScreeningQuery input) { throw new FeatureNotImplementedException("GetScreeningQuery"); }
}

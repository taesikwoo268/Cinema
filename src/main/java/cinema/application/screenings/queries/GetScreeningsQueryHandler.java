package cinema.application.screenings.queries;

import cinema.application.common.interfaces.UseCase;
import cinema.application.common.models.*;
import cinema.application.common.exceptions.FeatureNotImplementedException;
import java.util.List;
/** TODO: implement orchestration through application ports; see docs/architecture-contract.md. */
public final class GetScreeningsQueryHandler implements UseCase<GetScreeningsQuery, List<ScreeningView>> {
    @Override public List<ScreeningView> handle(GetScreeningsQuery input) { throw new FeatureNotImplementedException("GetScreeningsQuery"); }
}

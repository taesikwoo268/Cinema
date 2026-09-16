package cinema.application.screenings.queries;

import cinema.application.common.interfaces.UseCase;
import cinema.application.common.models.*;
import cinema.application.common.exceptions.FeatureNotImplementedException;
import java.util.List;
/** TODO: implement orchestration through application ports; see docs/architecture-contract.md. */
public final class GetAvailableSeatsQueryHandler implements UseCase<GetAvailableSeatsQuery, List<SeatView>> {
    @Override public List<SeatView> handle(GetAvailableSeatsQuery input) { throw new FeatureNotImplementedException("GetAvailableSeatsQuery"); }
}

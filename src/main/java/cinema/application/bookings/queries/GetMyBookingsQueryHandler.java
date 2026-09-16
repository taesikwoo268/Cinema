package cinema.application.bookings.queries;

import cinema.application.common.interfaces.UseCase;
import cinema.application.common.models.*;
import cinema.application.common.exceptions.FeatureNotImplementedException;
import java.util.List;
/** TODO: implement orchestration through application ports; see docs/architecture-contract.md. */
public final class GetMyBookingsQueryHandler implements UseCase<GetMyBookingsQuery, List<BookingView>> {
    @Override public List<BookingView> handle(GetMyBookingsQuery input) { throw new FeatureNotImplementedException("GetMyBookingsQuery"); }
}

package cinema.application.tickets.queries;

import cinema.application.common.interfaces.UseCase;
import cinema.application.common.models.*;
import cinema.application.common.exceptions.FeatureNotImplementedException;
import java.util.List;
/** TODO: implement orchestration through application ports; see docs/architecture-contract.md. */
public final class GetMyTicketsQueryHandler implements UseCase<GetMyTicketsQuery, List<TicketView>> {
    @Override public List<TicketView> handle(GetMyTicketsQuery input) { throw new FeatureNotImplementedException("GetMyTicketsQuery"); }
}

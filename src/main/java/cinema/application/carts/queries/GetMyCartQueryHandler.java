package cinema.application.carts.queries;

import cinema.application.common.interfaces.UseCase;
import cinema.application.common.models.*;
import cinema.application.common.exceptions.FeatureNotImplementedException;
import java.util.List;
/** TODO: implement orchestration through application ports; see docs/architecture-contract.md. */
public final class GetMyCartQueryHandler implements UseCase<GetMyCartQuery, CartView> {
    @Override public CartView handle(GetMyCartQuery input) { throw new FeatureNotImplementedException("GetMyCartQuery"); }
}

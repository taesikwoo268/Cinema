package cinema.application.movies.queries;

import cinema.application.common.interfaces.UseCase;
import cinema.application.common.models.*;
import cinema.application.common.exceptions.FeatureNotImplementedException;
import java.util.List;
/** TODO: implement orchestration through application ports; see docs/architecture-contract.md. */
public final class GetMoviesQueryHandler implements UseCase<GetMoviesQuery, List<MovieView>> {
    @Override public List<MovieView> handle(GetMoviesQuery input) { throw new FeatureNotImplementedException("GetMoviesQuery"); }
}

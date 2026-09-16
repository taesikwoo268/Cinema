package cinema.application.movies.queries;

import cinema.application.common.interfaces.UseCase;
import cinema.application.common.models.*;
import cinema.application.common.exceptions.FeatureNotImplementedException;
import java.util.List;
/** TODO: implement orchestration through application ports; see docs/architecture-contract.md. */
public final class GetMovieQueryHandler implements UseCase<GetMovieQuery, MovieView> {
    @Override public MovieView handle(GetMovieQuery input) { throw new FeatureNotImplementedException("GetMovieQuery"); }
}

package cinema.application.movies.commands;

import cinema.application.common.interfaces.UseCase;
import cinema.application.common.models.*;
import cinema.application.common.exceptions.FeatureNotImplementedException;
import java.util.List;
/** TODO: implement orchestration through application ports; see docs/architecture-contract.md. */
public final class CreateMovieCommandHandler implements UseCase<CreateMovieCommand, String> {
    @Override public String handle(CreateMovieCommand input) { throw new FeatureNotImplementedException("CreateMovieCommand"); }
}

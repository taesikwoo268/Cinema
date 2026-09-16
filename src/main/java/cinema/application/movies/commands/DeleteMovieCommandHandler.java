package cinema.application.movies.commands;

import cinema.application.common.interfaces.UseCase;
import cinema.application.common.models.*;
import cinema.application.common.exceptions.FeatureNotImplementedException;
import java.util.List;
/** TODO: implement orchestration through application ports; see docs/architecture-contract.md. */
public final class DeleteMovieCommandHandler implements UseCase<DeleteMovieCommand, Void> {
    @Override public Void handle(DeleteMovieCommand input) { throw new FeatureNotImplementedException("DeleteMovieCommand"); }
}

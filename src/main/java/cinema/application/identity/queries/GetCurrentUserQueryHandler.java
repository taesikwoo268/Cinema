package cinema.application.identity.queries;

import cinema.application.common.exceptions.ValidationException;
import cinema.application.common.interfaces.CurrentUser;
import cinema.application.common.interfaces.UseCase;
import cinema.application.common.models.CurrentUserView;

/** Returns the authentication state exposed through the current-user port. */
public final class GetCurrentUserQueryHandler implements UseCase<GetCurrentUserQuery, CurrentUserView> {
    private final CurrentUser currentUser;

    public GetCurrentUserQueryHandler(CurrentUser currentUser) {
        this.currentUser = currentUser;
    }

    @Override
    public CurrentUserView handle(GetCurrentUserQuery input) {
        if (input == null) {
            throw new ValidationException("GetCurrentUserQuery must not be null");
        }
        if (!currentUser.isAuthenticated()) {
            return new CurrentUserView(null, false);
        }
        return new CurrentUserView(currentUser.getUserId(), true);
    }
}

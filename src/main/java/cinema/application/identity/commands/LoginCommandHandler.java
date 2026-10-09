package cinema.application.identity.commands;

import cinema.application.common.exceptions.ValidationException;
import cinema.application.common.interfaces.IdentityService;
import cinema.application.common.interfaces.UseCase;
import cinema.application.common.models.AuthenticationResult;

/** Authenticates a user through the identity port. */
public class LoginCommandHandler implements UseCase<LoginCommand, AuthenticationResult> {
    private final IdentityService identityService;

    public LoginCommandHandler(IdentityService identityService) {
        this.identityService = identityService;
    }

    @Override
    public AuthenticationResult handle(LoginCommand input) {
        if (input == null) {
            throw new ValidationException("LoginCommand must not be null");
        }
        if (input.email() == null || input.email().isBlank()) {
            throw new ValidationException("Login email must not be blank");
        }
        if (input.password() == null || input.password().isBlank()) {
            throw new ValidationException("Login password must not be blank");
        }
        return identityService.login(input.email(), input.password());
    }
}

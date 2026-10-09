package cinema.web.controllers;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import cinema.application.common.models.AuthenticationResult;
import cinema.application.common.models.CurrentUserView;
import cinema.application.identity.commands.LoginCommand;
import cinema.application.identity.commands.LoginCommandHandler;
import cinema.application.identity.commands.RegisterCommand;
import cinema.application.identity.commands.RegisterCommandHandler;
import cinema.application.identity.queries.GetCurrentUserQuery;
import cinema.application.identity.queries.GetCurrentUserQueryHandler;
import cinema.web.requests.LoginRequest;
import cinema.web.requests.RegisterRequest;
import cinema.web.responses.CreatedResourceResponse;

@RestController
@Profile({"venue-dev", "mysql"})
@RequestMapping("/identity")
public final class IdentityController {

    private final RegisterCommandHandler registerCommand;
    private final LoginCommandHandler loginCommand;
    private final GetCurrentUserQueryHandler getCurrentUserQuery;

    public IdentityController(
            RegisterCommandHandler registerCommand,
            LoginCommandHandler loginCommand,
            GetCurrentUserQueryHandler getCurrentUserQuery) {
        this.registerCommand = registerCommand;
        this.loginCommand = loginCommand;
        this.getCurrentUserQuery = getCurrentUserQuery;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public CreatedResourceResponse register(@RequestBody RegisterRequest request) {
        String customerId = registerCommand.handle(
                new RegisterCommand(
                        request.email(),
                        request.password(),
                        request.displayName()));

        return new CreatedResourceResponse(customerId);
    }

    @PostMapping("/login")
    public AuthenticationResult login(@RequestBody LoginRequest request) {
        return loginCommand.handle(
                new LoginCommand(
                        request.email(),
                        request.password()));
    }

    @GetMapping("/me")
    public CurrentUserView currentUser() {
        return getCurrentUserQuery.handle(new GetCurrentUserQuery());
    }
}
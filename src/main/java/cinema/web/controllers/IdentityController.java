package cinema.web.controllers;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cinema.application.identity.commands.LoginCommandHandler;
import cinema.application.identity.commands.RegisterCommandHandler;
import cinema.application.identity.queries.GetCurrentUserQueryHandler;

/** HTTP methods, request mapping and response mapping are pending implementation. */
@RestController
@Profile("venue-dev")
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
}
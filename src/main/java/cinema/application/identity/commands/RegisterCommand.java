package cinema.application.identity.commands;

/**
 * Input contract only. Validation is pending.
 */
public record RegisterCommand(
        String email,
        String password,
        String displayName
        ) {

}

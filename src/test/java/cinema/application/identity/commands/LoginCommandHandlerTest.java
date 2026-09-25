package cinema.application.identity.commands;

import cinema.application.common.exceptions.ValidationException;
import cinema.application.common.interfaces.IdentityService;
import cinema.application.common.models.AuthenticationResult;
import java.time.Instant;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class LoginCommandHandlerTest {
    private final IdentityService identityService = mock(IdentityService.class);
    private final LoginCommandHandler handler = new LoginCommandHandler(identityService);

    @Test
    void returnsAuthenticationResultFromIdentityService() {
        AuthenticationResult expected = new AuthenticationResult("access-token", Instant.parse("2026-09-26T00:00:00Z"));
        when(identityService.login("user@example.com", "password")).thenReturn(expected);

        assertSame(expected, handler.handle(new LoginCommand("user@example.com", "password")));

        verify(identityService).login("user@example.com", "password");
    }

    @Test
    void propagatesIdentityServiceFailure() {
        IllegalArgumentException failure = new IllegalArgumentException("Invalid credentials");
        when(identityService.login("user@example.com", "password")).thenThrow(failure);

        assertSame(failure, assertThrows(IllegalArgumentException.class,
                () -> handler.handle(new LoginCommand("user@example.com", "password"))));
    }

    @Test
    void rejectsInvalidInputBeforeCallingIdentityService() {
        assertInvalid(null);
        assertInvalid(new LoginCommand(null, "password"));
        assertInvalid(new LoginCommand(" \t", "password"));
        assertInvalid(new LoginCommand("user@example.com", null));
        assertInvalid(new LoginCommand("user@example.com", "\n "));
    }

    private void assertInvalid(LoginCommand command) {
        assertThrows(ValidationException.class, () -> handler.handle(command));
        verifyNoInteractions(identityService);
    }
}

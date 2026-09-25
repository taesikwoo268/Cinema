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
    void validCommandReturnsAuthenticationResult() {
        var expected = new AuthenticationResult("token-abc", Instant.now().plusSeconds(3600));
        when(identityService.login("a@b.com", "pass")).thenReturn(expected);

        var result = handler.handle(new LoginCommand("a@b.com", "pass"));

        assertSame(expected, result);
        verify(identityService).login("a@b.com", "pass");
    }

    @Test
    void nullCommandRejected() {
        assertThrows(ValidationException.class, () -> handler.handle(null));
        verifyNoInteractions(identityService);
    }

    @Test
    void blankEmailRejectedBeforeSideEffects() {
        assertThrows(ValidationException.class,
                () -> handler.handle(new LoginCommand("  ", "pass")));
        verifyNoInteractions(identityService);
    }

    @Test
    void blankPasswordRejectedBeforeSideEffects() {
        assertThrows(ValidationException.class,
                () -> handler.handle(new LoginCommand("a@b.com", "")));
        verifyNoInteractions(identityService);
    }

    @Test
    void invalidCredentialPropagates() {
        when(identityService.login("a@b.com", "wrong"))
                .thenThrow(new RuntimeException("invalid credentials"));

        assertThrows(RuntimeException.class,
                () -> handler.handle(new LoginCommand("a@b.com", "wrong")));
    }
}

package cinema.infrastructure.identity;

import cinema.application.common.exceptions.ConflictException;
import cinema.application.common.exceptions.ForbiddenException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IdentityServiceImplTest {
    private static final Instant NOW = Instant.parse("2026-09-26T00:00:00Z");

    @Test
    void registersAccountWithGeneratedUserId() {
        IdentityServiceImpl identity = identityService();

        assertEquals("user-1", identity.register("user@example.com", "password"));
    }

    @Test
    void rejectsDuplicateEmail() {
        IdentityServiceImpl identity = identityService();
        identity.register("user@example.com", "password");

        assertThrows(ConflictException.class, () -> identity.register("user@example.com", "another-password"));
    }

    @Test
    void authenticatesValidCredentialsWithNonBlankTokenAndFutureExpiry() {
        IdentityServiceImpl identity = identityService();
        identity.register("user@example.com", "password");

        var result = identity.login("user@example.com", "password");

        assertNotNull(result.accessToken());
        assertFalse(result.accessToken().isBlank());
        assertTrue(result.expiresAt().isAfter(NOW));
    }

    @Test
    void rejectsUnknownEmailAndWrongPassword() {
        IdentityServiceImpl identity = identityService();
        identity.register("user@example.com", "password");

        assertThrows(ForbiddenException.class, () -> identity.login("unknown@example.com", "password"));
        assertThrows(ForbiddenException.class, () -> identity.login("user@example.com", "wrong-password"));
    }

    @Test
    void applicationUserDoesNotExposePasswordHashInToString() {
        ApplicationUser user = new ApplicationUser("user-1", "user@example.com", "sensitive-password-hash");

        assertFalse(user.toString().contains("sensitive-password-hash"));
    }

    private static IdentityServiceImpl identityService() {
        return new IdentityServiceImpl(() -> "user-1", Clock.fixed(NOW, ZoneOffset.UTC));
    }
}

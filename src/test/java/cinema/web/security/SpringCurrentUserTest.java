package cinema.web.security;

import cinema.application.common.exceptions.ForbiddenException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;

class SpringCurrentUserTest {

    private final SpringCurrentUser currentUser = new SpringCurrentUser();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authenticatedPrincipalMapsToUserId() {
        var auth = new UsernamePasswordAuthenticationToken(
                "user-123", null, AuthorityUtils.NO_AUTHORITIES);
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertTrue(currentUser.isAuthenticated());
        assertEquals("user-123", currentUser.getUserId());
    }

    @Test
    void anonymousAuthenticationIsUnauthenticatedRegardlessOfPrincipalName() {
        var auth = new AnonymousAuthenticationToken(
                "key", "anonymous-principal",
                AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertFalse(currentUser.isAuthenticated());
        assertThrows(ForbiddenException.class, currentUser::getUserId);
    }

    @Test
    void missingAuthenticationIsUnauthenticated() {
        SecurityContextHolder.clearContext();

        assertFalse(currentUser.isAuthenticated());
        assertThrows(ForbiddenException.class, currentUser::getUserId);
    }
}

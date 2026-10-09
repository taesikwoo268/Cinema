package cinema.web.security;

import cinema.application.common.interfaces.TokenVerifier;
import cinema.application.common.models.AuthenticatedUser;
import java.util.Optional;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerExceptionResolver;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BearerAuthenticationFilterTest {
    @AfterEach void cleanup() { SecurityContextHolder.clearContext(); }

    @Test void principalAndRoleAreAvailableOnlyDuringRequestEvenIfHandlerFails() {
        TokenVerifier verifier = token -> Optional.of(new AuthenticatedUser("user-1", "ADMIN"));
        var filter = new BearerAuthenticationFilter(verifier, mock(HandlerExceptionResolver.class));
        var request = new MockHttpServletRequest(); request.addHeader("Authorization", "Bearer valid");
        assertThrows(ServletException.class, () -> filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            assertEquals("user-1", auth.getName());
            assertEquals("ROLE_ADMIN", auth.getAuthorities().iterator().next().getAuthority());
            assertNull(auth.getCredentials());
            throw new ServletException("handler failed");
        }));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}

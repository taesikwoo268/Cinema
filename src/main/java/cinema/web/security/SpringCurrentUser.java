package cinema.web.security;

import cinema.application.common.interfaces.CurrentUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
public final class SpringCurrentUser implements CurrentUser {
    @Override public boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
    }
    @Override public String getUserId() {
        if (!isAuthenticated()) { throw new cinema.application.common.exceptions.ForbiddenException("Authentication required"); }
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}

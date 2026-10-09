package cinema.web.security;

import cinema.application.common.exceptions.ForbiddenException;
import cinema.application.common.interfaces.TokenVerifier;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

// Registered only in the MySQL security chain, never as a servlet filter bean.
public final class BearerAuthenticationFilter extends OncePerRequestFilter {
    private final TokenVerifier verifier;
    private final HandlerExceptionResolver errors;
    public BearerAuthenticationFilter(TokenVerifier verifier, HandlerExceptionResolver errors) {
        this.verifier = verifier; this.errors = errors;
    }

    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        var headers = Collections.list(request.getHeaders("Authorization"));
        if (headers.isEmpty()) { chain.doFilter(request, response); return; }
        if (headers.size() != 1 || !headers.getFirst().regionMatches(true, 0, "Bearer ", 0, 7)) {
            reject(request, response); return;
        }
        var verified = verifier.verify(headers.getFirst().substring(7));
        if (verified.isEmpty()) { reject(request, response); return; }
        var user = verified.get();
        var auth = new UsernamePasswordAuthenticationToken(user.userId(), null,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.role())));
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        try { chain.doFilter(request, response); }
        finally { SecurityContextHolder.clearContext(); }
    }

    private void reject(HttpServletRequest request, HttpServletResponse response) {
        SecurityContextHolder.clearContext();
        errors.resolveException(request, response, null,
                new ForbiddenException("Invalid or expired access token"));
    }
}

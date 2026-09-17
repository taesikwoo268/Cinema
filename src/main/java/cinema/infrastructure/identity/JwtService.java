package cinema.infrastructure.identity;

import java.util.Optional;

import cinema.application.common.interfaces.TokenVerifier;
import cinema.application.common.models.AuthenticatedUser;

public final class JwtService implements TokenVerifier {

    @Override
    public Optional<AuthenticatedUser> verify(String token) {
        throw new UnsupportedOperationException("JWT verification pending");
    }
}

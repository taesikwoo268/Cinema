package cinema.infrastructure.identity;

import cinema.application.common.interfaces.IdentityService;
import cinema.application.common.models.*;
import cinema.domain.common.Money;
/** Adapter skeleton; not registered as a working provider. */
public final class IdentityServiceImpl implements IdentityService {
    @Override public String register(String email, String password) { throw new UnsupportedOperationException("Architecture Contract v0.1: not implemented" ); }
    @Override public AuthenticationResult login(String email, String password) { throw new UnsupportedOperationException("Architecture Contract v0.1: not implemented" ); }
}

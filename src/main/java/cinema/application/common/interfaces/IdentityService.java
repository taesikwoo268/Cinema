package cinema.application.common.interfaces;

import cinema.application.common.models.AuthenticationResult;

public interface IdentityService {

    String register(String email, String password);

    AuthenticationResult login(String email, String password);
}

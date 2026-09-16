package cinema.application.common.interfaces;

import cinema.application.common.models.*;
import cinema.domain.common.Money;
import java.time.Instant;
import java.util.Optional;
public interface IdentityService { String register(String email, String password); AuthenticationResult login(String email, String password); }

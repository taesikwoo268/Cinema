package cinema.application.common.interfaces;

import cinema.application.common.models.*;
import cinema.domain.common.Money;
import java.time.Instant;
import java.util.Optional;
public interface CurrentUser { String getUserId(); boolean isAuthenticated(); }

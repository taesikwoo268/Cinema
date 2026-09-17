package cinema.application.common.models;

import java.time.Instant;

public record AuthenticationResult(String accessToken, Instant expiresAt) {

}

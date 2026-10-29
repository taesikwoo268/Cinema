package cinema.application.common.models;

import java.time.Instant;

public record AuthenticationResult(String accessToken, Instant expiresAt) {

    @Override
    public String toString() {
        return "AuthenticationResult[accessToken=redacted, expiresAt=" + expiresAt + "]";
    }
}

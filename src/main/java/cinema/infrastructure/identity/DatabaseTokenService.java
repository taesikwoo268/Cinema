package cinema.infrastructure.identity;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cinema.application.common.interfaces.Clock;
import cinema.application.common.interfaces.TokenVerifier;
import cinema.application.common.models.AuthenticatedUser;
import cinema.application.common.models.AuthenticationResult;
import cinema.infrastructure.persistence.entities.AccessTokenJpaEntity;
import cinema.infrastructure.persistence.entities.UserJpaEntity;
import cinema.infrastructure.persistence.repositories.SpringDataAccessTokenRepository;

@Service
@Profile("mysql")
public class DatabaseTokenService implements TokenVerifier {
    private static final Pattern TOKEN_FORMAT = Pattern.compile("[A-Za-z0-9_-]{43}");
    private final SpringDataAccessTokenRepository tokens;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();

    public DatabaseTokenService(SpringDataAccessTokenRepository tokens, Clock clock) {
        this.tokens = tokens; this.clock = clock;
    }

    @Transactional
    public AuthenticationResult issue(UserJpaEntity user) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        var expiresAt = clock.now().plusSeconds(3600).truncatedTo(ChronoUnit.MICROS);
        var entity = new AccessTokenJpaEntity();
        entity.tokenHash = hash(raw); entity.user = user; entity.expiresAt = expiresAt;
        tokens.saveAndFlush(entity);
        return new AuthenticationResult(raw, expiresAt);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AuthenticatedUser> verify(String token) {
        if (token == null || !TOKEN_FORMAT.matcher(token).matches()) return Optional.empty();
        var now = clock.now();
        return tokens.findByTokenHash(hash(token))
                .filter(saved -> saved.expiresAt.isAfter(now))
                .map(saved -> new AuthenticatedUser(saved.user.id, saved.user.role));
    }

    private static String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.US_ASCII)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 unavailable", ex);
        }
    }
}



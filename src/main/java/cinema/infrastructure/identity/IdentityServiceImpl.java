package cinema.infrastructure.identity;

import cinema.application.common.exceptions.ConflictException;
import cinema.application.common.exceptions.ForbiddenException;
import cinema.application.common.interfaces.IdentityService;
import cinema.application.common.models.AuthenticationResult;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * In-memory identity adapter for the M4 development slice.
 *
 * <p>This is deliberately not production authentication: accounts and tokens are
 * lost on restart, and token verification is supplied by a later slice.</p>
 */
public final class IdentityServiceImpl implements IdentityService {
    private static final Duration TOKEN_LIFETIME = Duration.ofHours(1);

    private final ConcurrentHashMap<String, ApplicationUser> usersByEmail = new ConcurrentHashMap<>();
    private final Supplier<String> userIdGenerator;
    private final Clock clock;

    public IdentityServiceImpl() {
        this(() -> UUID.randomUUID().toString(), Clock.systemUTC());
    }

    public IdentityServiceImpl(Supplier<String> userIdGenerator, Clock clock) {
        this.userIdGenerator = userIdGenerator;
        this.clock = clock;
    }

    @Override
    public String register(String email, String password) {
        ApplicationUser user = new ApplicationUser(userIdGenerator.get(), email, hash(password));
        ApplicationUser existing = usersByEmail.putIfAbsent(email, user);
        if (existing != null) {
            throw new ConflictException("Email already registered: " + email);
        }
        return user.id();
    }

    @Override
    public AuthenticationResult login(String email, String password) {
        ApplicationUser user = usersByEmail.get(email);
        if (user == null || !MessageDigest.isEqual(
                user.passwordHash().getBytes(StandardCharsets.UTF_8), hash(password).getBytes(StandardCharsets.UTF_8))) {
            throw new ForbiddenException("Invalid credentials");
        }

        Instant expiresAt = clock.instant().plus(TOKEN_LIFETIME);
        return new AuthenticationResult(UUID.randomUUID().toString(), expiresAt);
    }

    private static String hash(String password) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(password.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }
}

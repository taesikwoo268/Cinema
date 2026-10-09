package cinema.infrastructure.identity;

import cinema.application.common.exceptions.*;
import cinema.application.common.interfaces.IdentityService;
import cinema.application.common.models.AuthenticationResult;
import cinema.infrastructure.persistence.entities.UserJpaEntity;
import cinema.infrastructure.persistence.repositories.SpringDataUserRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import java.time.Instant;
import java.util.UUID;

@Service
@Profile("mysql")
@Transactional
public class JpaIdentityService implements IdentityService {
    private final SpringDataUserRepository users;
    private final Pbkdf2PasswordEncoder passwords = Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    public JpaIdentityService(SpringDataUserRepository users) { this.users = users; }
    @Override public String register(String email, String password) {
        if (users.findByEmail(email).isPresent()) throw new ConflictException("Email already registered: " + email);
        var user = new UserJpaEntity();
        user.id = UUID.randomUUID().toString(); user.email = email; user.passwordHash = passwords.encode(password);
        users.saveAndFlush(user);
        return user.id;
    }
    @Override public AuthenticationResult login(String email, String password) {
        var user = users.findByEmail(email).orElseThrow(() -> new ForbiddenException("Invalid credentials"));
        if (!passwords.matches(password, user.passwordHash)) throw new ForbiddenException("Invalid credentials");
        return new AuthenticationResult(UUID.randomUUID().toString(), Instant.now().plusSeconds(3600));
    }
}

package cinema.infrastructure.identity;

import cinema.application.common.interfaces.Clock;
import cinema.infrastructure.persistence.entities.AccessTokenJpaEntity;
import cinema.infrastructure.persistence.entities.UserJpaEntity;
import cinema.infrastructure.persistence.repositories.SpringDataAccessTokenRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DatabaseTokenServiceTest {
    @Test void expiryBoundaryAndRoleAreReadFromPersistedRecord() {
        var repository = mock(SpringDataAccessTokenRepository.class);
        var clock = mock(Clock.class);
        var now = Instant.parse("2030-01-01T00:00:00Z");
        when(clock.now()).thenReturn(now);
        var service = new DatabaseTokenService(repository, clock);
        var user = new UserJpaEntity(); user.id = "user";
        var token = service.issue(user);
        var saved = ArgumentCaptor.forClass(AccessTokenJpaEntity.class);
        verify(repository).saveAndFlush(saved.capture());
        assertEquals(43, token.accessToken().length());
        assertFalse(token.toString().contains(token.accessToken()));
        assertEquals(now.plusSeconds(3600), token.expiresAt());
        assertNotEquals(token.accessToken(), saved.getValue().tokenHash);
        when(repository.findByTokenHash(saved.getValue().tokenHash)).thenReturn(Optional.of(saved.getValue()));
        when(clock.now()).thenReturn(token.expiresAt().minusNanos(1));
        assertEquals("CUSTOMER", service.verify(token.accessToken()).orElseThrow().role());
        user.role = "ADMIN";
        assertEquals("ADMIN", service.verify(token.accessToken()).orElseThrow().role());
        when(clock.now()).thenReturn(token.expiresAt());
        assertTrue(service.verify(token.accessToken()).isEmpty());
        when(clock.now()).thenReturn(token.expiresAt().plusSeconds(1));
        assertTrue(service.verify(token.accessToken()).isEmpty());
    }

    @Test void malformedTokensNeverReachDatabaseAndDatabaseFailuresPropagate() {
        var repository = mock(SpringDataAccessTokenRepository.class);
        var service = new DatabaseTokenService(repository, () -> Instant.EPOCH);
        for (String token : new String[]{null, "", "x", "x".repeat(44), "!".repeat(43)}) {
            assertTrue(service.verify(token).isEmpty());
        }
        verifyNoInteractions(repository);
        when(repository.findByTokenHash(anyString())).thenThrow(new IllegalStateException("Database offline"));
        assertThrows(IllegalStateException.class, () -> service.verify("x".repeat(43)));
    }
}

package cinema.application.identity.queries;

import cinema.application.common.exceptions.ValidationException;
import cinema.application.common.interfaces.CurrentUser;
import cinema.application.common.models.CurrentUserView;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class GetCurrentUserQueryHandlerTest {
    private final CurrentUser currentUser = mock(CurrentUser.class);
    private final GetCurrentUserQueryHandler handler = new GetCurrentUserQueryHandler(currentUser);

    @Test
    void returnsAuthenticatedUserId() {
        when(currentUser.isAuthenticated()).thenReturn(true);
        when(currentUser.getUserId()).thenReturn("user-1");

        assertEquals(new CurrentUserView("user-1", true), handler.handle(new GetCurrentUserQuery()));

        verify(currentUser).isAuthenticated();
        verify(currentUser).getUserId();
    }

    @Test
    void returnsUnauthenticatedViewWithoutReadingUserId() {
        when(currentUser.isAuthenticated()).thenReturn(false);

        assertEquals(new CurrentUserView(null, false), handler.handle(new GetCurrentUserQuery()));

        verify(currentUser).isAuthenticated();
        verify(currentUser, never()).getUserId();
    }

    @Test
    void rejectsNullQueryBeforeReadingCurrentUser() {
        assertThrows(ValidationException.class, () -> handler.handle(null));

        verifyNoInteractions(currentUser);
    }

    @Test
    void correctUserIdMapping() {
        when(currentUser.isAuthenticated()).thenReturn(true);
        when(currentUser.getUserId()).thenReturn("abc-xyz-789");

        CurrentUserView view = handler.handle(new GetCurrentUserQuery());

        assertEquals("abc-xyz-789", view.userId());
        assertTrue(view.authenticated());
    }
}

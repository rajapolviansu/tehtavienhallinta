package fi.app.tehtavienhallinta;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Käyttäjien hallinta on sallittu vain ADMIN-roolille.
 */
@SpringBootTest
@Transactional
class AppUserServiceSecurityTest {

    private static final String PASSWORD = UUID.randomUUID().toString();

    @Autowired
    AppUserService service;

    @Test
    @WithMockUser(roles = "USER")
    void regularUserIsDeniedEverything() {
        assertThrows(AccessDeniedException.class, () -> service.findAll());
        assertThrows(AccessDeniedException.class, () -> service.get(1L));
        assertThrows(AccessDeniedException.class,
                () -> service.create("uusi", "uusi@example.com", PASSWORD));
        assertThrows(AccessDeniedException.class,
                () -> service.update(1L, "uusi", "uusi@example.com"));
        assertThrows(AccessDeniedException.class, () -> service.delete(1L));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminIsAllowed() {
        AppUser u = assertDoesNotThrow(() -> service.create("uusi", "uusi@example.com", PASSWORD));

        assertTrue(service.findAll().stream().anyMatch(x -> x.getId().equals(u.getId())));
        assertDoesNotThrow(() -> service.delete(u.getId()));
    }

    @Test
    void anonymousIsDenied() {
        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> service.findAll());
    }
}

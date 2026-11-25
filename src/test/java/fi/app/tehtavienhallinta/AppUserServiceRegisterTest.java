package fi.app.tehtavienhallinta;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Itserekisteröityminen on avoin kaikille, mutta luo aina tavallisen käyttäjän.
 */
@SpringBootTest
@Transactional
class AppUserServiceRegisterTest {

    private static final String PASSWORD = UUID.randomUUID().toString();

    @Autowired
    AppUserService service;

    @Autowired
    AppUserRepository users;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    @WithAnonymousUser
    void anonymousCanRegisterAsUserWithHashedPassword() {
        AppUser u = service.register("rekisteroity", "rekisteroity@example.com", PASSWORD);

        assertNotNull(u.getId());
        assertEquals(Role.USER, u.getRole());
        assertNotEquals(PASSWORD, u.getPasswordHash());
        assertTrue(passwordEncoder.matches(PASSWORD, u.getPasswordHash()));
        assertTrue(users.existsByUsername("rekisteroity"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void registerNeverCreatesAdmin() {
        AppUser u = service.register("rekisteroity", "rekisteroity@example.com", PASSWORD);

        assertEquals(Role.USER, u.getRole());
    }

    @Test
    @WithAnonymousUser
    void registerRejectsShortOrMissingPassword() {
        assertThrows(IllegalArgumentException.class,
                () -> service.register("rekisteroity", "rekisteroity@example.com", "lyhyt"));
        assertThrows(IllegalArgumentException.class,
                () -> service.register("rekisteroity", "rekisteroity@example.com", null));
    }

    @Test
    @WithAnonymousUser
    void registerRejectsDuplicateUsernameAndEmail() {
        service.register("rekisteroity", "rekisteroity@example.com", PASSWORD);

        assertThrows(IllegalArgumentException.class,
                () -> service.register("rekisteroity", "toinen@example.com", PASSWORD));
        assertThrows(IllegalArgumentException.class,
                () -> service.register("toinen", "rekisteroity@example.com", PASSWORD));
    }

    @Test
    @WithAnonymousUser
    void registeringDoesNotOpenOtherOperations() {
        assertThrows(AccessDeniedException.class,
                () -> service.create("admin2", "admin2@example.com", PASSWORD, Role.ADMIN));
        assertThrows(AccessDeniedException.class, () -> service.findAll());
    }
}

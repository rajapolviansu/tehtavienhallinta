package fi.app.tehtavienhallinta;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AppUserServiceTest {

    // Satunnainen salasana joka ajolla, ei kovakoodattua arvoa
    private static final String PASSWORD = UUID.randomUUID().toString();

    @Autowired
    AppUserService service;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    void passwordIsStoredAsHash() {
        AppUser u = service.create("testaaja", "testaaja@example.com", PASSWORD);

        assertNotEquals(PASSWORD, u.getPasswordHash());
        assertTrue(passwordEncoder.matches(PASSWORD, u.getPasswordHash()));
        assertFalse(passwordEncoder.matches("vaara-salasana", u.getPasswordHash()));
    }

    @Test
    void createRejectsTooShortOrMissingPassword() {
        assertThrows(IllegalArgumentException.class,
                () -> service.create("testaaja", "testaaja@example.com", "lyhyt"));
        assertThrows(IllegalArgumentException.class,
                () -> service.create("testaaja", "testaaja@example.com", null));
    }

    @Test
    void createAndGet() {
        AppUser u = service.create("testaaja", "testaaja@example.com", PASSWORD);

        assertNotNull(u.getId());
        assertEquals("testaaja", service.get(u.getId()).getUsername());
        assertTrue(service.findAll().stream().anyMatch(x -> x.getId().equals(u.getId())));
    }

    @Test
    void createRejectsDuplicateUsernameAndEmail() {
        service.create("testaaja", "testaaja@example.com", PASSWORD);

        assertThrows(IllegalArgumentException.class,
                () -> service.create("testaaja", "toinen@example.com", PASSWORD));
        assertThrows(IllegalArgumentException.class,
                () -> service.create("toinen", "testaaja@example.com", PASSWORD));
    }

    @Test
    void updateChangesFields() {
        AppUser u = service.create("testaaja", "testaaja@example.com", PASSWORD);

        service.update(u.getId(), "uusi", "uusi@example.com");

        AppUser found = service.get(u.getId());
        assertEquals("uusi", found.getUsername());
        assertEquals("uusi@example.com", found.getEmail());
    }

    @Test
    void updateAllowsKeepingOwnValuesButRejectsOthers() {
        AppUser a = service.create("a-user", "a@example.com", PASSWORD);
        service.create("b-user", "b@example.com", PASSWORD);

        assertDoesNotThrow(() -> service.update(a.getId(), "a-user", "a2@example.com"));
        assertThrows(IllegalArgumentException.class,
                () -> service.update(a.getId(), "b-user", "a2@example.com"));
    }

    @Test
    void deleteRemovesUser() {
        AppUser u = service.create("testaaja", "testaaja@example.com", PASSWORD);

        service.delete(u.getId());

        assertThrows(EntityNotFoundException.class, () -> service.get(u.getId()));
    }

    @Test
    void getMissingThrows() {
        assertThrows(EntityNotFoundException.class, () -> service.get(-1L));
    }
}

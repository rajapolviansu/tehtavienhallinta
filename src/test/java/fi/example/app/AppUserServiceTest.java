package fi.example.app;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AppUserServiceTest {

    @Autowired
    AppUserService service;

    @Test
    void createAndGet() {
        AppUser u = service.create("testaaja", "testaaja@example.com");

        assertNotNull(u.getId());
        assertEquals("testaaja", service.get(u.getId()).getUsername());
        assertTrue(service.findAll().stream().anyMatch(x -> x.getId().equals(u.getId())));
    }

    @Test
    void createRejectsDuplicateUsernameAndEmail() {
        service.create("testaaja", "testaaja@example.com");

        assertThrows(IllegalArgumentException.class,
                () -> service.create("testaaja", "toinen@example.com"));
        assertThrows(IllegalArgumentException.class,
                () -> service.create("toinen", "testaaja@example.com"));
    }

    @Test
    void updateChangesFields() {
        AppUser u = service.create("testaaja", "testaaja@example.com");

        service.update(u.getId(), "uusi", "uusi@example.com");

        AppUser found = service.get(u.getId());
        assertEquals("uusi", found.getUsername());
        assertEquals("uusi@example.com", found.getEmail());
    }

    @Test
    void updateAllowsKeepingOwnValuesButRejectsOthers() {
        AppUser a = service.create("a-user", "a@example.com");
        service.create("b-user", "b@example.com");

        assertDoesNotThrow(() -> service.update(a.getId(), "a-user", "a2@example.com"));
        assertThrows(IllegalArgumentException.class,
                () -> service.update(a.getId(), "b-user", "a2@example.com"));
    }

    @Test
    void deleteRemovesUser() {
        AppUser u = service.create("testaaja", "testaaja@example.com");

        service.delete(u.getId());

        assertThrows(EntityNotFoundException.class, () -> service.get(u.getId()));
    }

    @Test
    void getMissingThrows() {
        assertThrows(EntityNotFoundException.class, () -> service.get(-1L));
    }
}

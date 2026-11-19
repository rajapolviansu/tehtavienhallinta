package fi.example.app;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ProjectServiceTest {

    @Autowired
    ProjectService service;
    @Autowired
    TaskService tasks;
    @Autowired
    EntityManager em;

    private Project newProject(String name) {
        Project p = new Project();
        p.setName(name);
        return service.create(p);
    }

    @Test
    void createSetsIdAndTimestamps() {
        Project p = newProject("Uusi");

        assertNotNull(p.getId());
        assertNotNull(p.getCreatedAt());
        assertEquals("Uusi", service.get(p.getId()).getName());
    }

    @Test
    void updateChangesFields() {
        Project p = newProject("Vanha");
        Project changes = new Project();
        changes.setName("Uusi nimi");
        changes.setDescription("kuvaus");
        changes.setStartDate(LocalDate.of(2026, 1, 1));
        changes.setDueDate(LocalDate.of(2026, 2, 1));
        changes.setArchived(true);

        service.update(p.getId(), changes);

        Project found = service.get(p.getId());
        assertEquals("Uusi nimi", found.getName());
        assertEquals("kuvaus", found.getDescription());
        assertEquals(LocalDate.of(2026, 2, 1), found.getDueDate());
        assertTrue(found.isArchived());
    }

    @Test
    void findActiveExcludesArchived() {
        Project active = newProject("Aktiivinen");
        Project archived = newProject("Arkistoitu");
        archived.setArchived(true);

        var ids = service.findActive().stream().map(Project::getId).toList();

        assertTrue(ids.contains(active.getId()));
        assertFalse(ids.contains(archived.getId()));
        assertTrue(service.findAll().stream().anyMatch(x -> x.getId().equals(archived.getId())));
    }

    @Test
    void deleteRemovesProjectAndItsTasks() {
        Project p = newProject("Poistettava");
        Task t = new Task();
        t.setTitle("T");
        tasks.create(p.getId(), t);
        em.flush();

        service.delete(p.getId());
        em.flush();
        em.clear();

        assertThrows(EntityNotFoundException.class, () -> service.get(p.getId()));
        assertThrows(EntityNotFoundException.class, () -> tasks.get(t.getId()));
    }

    @Test
    void getMissingThrows() {
        assertThrows(EntityNotFoundException.class, () -> service.get(-1L));
    }
}

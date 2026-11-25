package fi.app.tehtavienhallinta;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class TagServiceTest {

    @Autowired
    TagService service;
    @Autowired
    ProjectService projects;
    @Autowired
    TaskService tasks;
    @Autowired
    EntityManager em;

    @Test
    void createAndGet() {
        Tag t = service.create("testitagi", "#ffffff");

        assertNotNull(t.getId());
        assertEquals("#ffffff", service.get(t.getId()).getColor());
        assertTrue(service.findAll().stream().anyMatch(x -> x.getId().equals(t.getId())));
    }

    @Test
    void createRejectsDuplicateNameIgnoringCase() {
        service.create("testitagi", "#cccccc");

        assertThrows(IllegalArgumentException.class, () -> service.create("TESTITAGI", "#cccccc"));
    }

    @Test
    void updateChangesFieldsAndRejectsExistingName() {
        Tag a = service.create("tagi-a", "#111111");
        service.create("tagi-b", "#cccccc");

        service.update(a.getId(), "tagi-a2", "#222222");
        assertEquals("tagi-a2", service.get(a.getId()).getName());
        assertEquals("#222222", service.get(a.getId()).getColor());

        assertThrows(IllegalArgumentException.class, () -> service.update(a.getId(), "TAGI-B", null));
    }

    @Test
    void deleteDetachesTagFromTasks() {
        Tag tag = service.create("poistettava", "#cccccc");
        Project p = new Project();
        p.setStartDate(java.time.LocalDate.of(2026, 1, 1));
        p.setDueDate(java.time.LocalDate.of(2026, 12, 31));
        p.setName("Projekti");
        projects.create(p);
        Task task = new Task();
        task.setDueDate(java.time.LocalDate.of(2026, 6, 1));
        task.setTitle("Tehtävä");
        tasks.create(p.getId(), task);
        tasks.addTag(task.getId(), tag.getId());
        em.flush();

        service.delete(tag.getId());
        em.flush();
        em.clear();

        assertThrows(EntityNotFoundException.class, () -> service.get(tag.getId()));
        assertTrue(tasks.get(task.getId()).getTags().isEmpty());
    }
}

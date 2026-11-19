package fi.app.tehtavienhallinta;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class TaskServiceTest {

    @Autowired
    TaskService service;
    @Autowired
    ProjectService projects;
    @Autowired
    TagService tags;
    @Autowired
    EntityManager em;

    Project project;

    @BeforeEach
    void setUp() {
        Project p = new Project();
        p.setName("Testiprojekti");
        project = projects.create(p);
    }

    private Task newTask(String title) {
        Task t = new Task();
        t.setTitle(title);
        return service.create(project.getId(), t);
    }

    @Test
    void createLinksTaskToProject() {
        Task t = newTask("Tehtävä");

        assertNotNull(t.getId());
        assertEquals(project.getId(), service.get(t.getId()).getProject().getId());
        assertEquals(1, service.findByProject(project.getId()).size());
    }

    @Test
    void createWithMissingProjectThrows() {
        Task t = new Task();
        t.setTitle("X");
        assertThrows(EntityNotFoundException.class, () -> service.create(-1L, t));
    }

    @Test
    void updateChangesFields() {
        Task t = newTask("Vanha");
        Task changes = new Task();
        changes.setTitle("Uusi");
        changes.setDescription("kuvaus");
        changes.setDueDate(LocalDate.of(2026, 5, 1));
        changes.setDone(true);

        service.update(t.getId(), changes);

        Task found = service.get(t.getId());
        assertEquals("Uusi", found.getTitle());
        assertEquals("kuvaus", found.getDescription());
        assertEquals(LocalDate.of(2026, 5, 1), found.getDueDate());
        assertTrue(found.isDone());
    }

    @Test
    void setDoneTogglesState() {
        Task t = newTask("T");

        assertTrue(service.setDone(t.getId(), true).isDone());
        assertFalse(service.setDone(t.getId(), false).isDone());
    }

    @Test
    void moveToProjectChangesProject() {
        Task t = newTask("T");
        Project other = new Project();
        other.setName("Toinen");
        projects.create(other);

        service.moveToProject(t.getId(), other.getId());
        em.flush();

        assertEquals(other.getId(), service.get(t.getId()).getProject().getId());
        assertTrue(service.findByProject(project.getId()).isEmpty());
        assertEquals(1, service.findByProject(other.getId()).size());
    }

    @Test
    void addAndRemoveTag() {
        Task t = newTask("T");
        Tag tag = tags.create("testitagi", null);

        service.addTag(t.getId(), tag.getId());
        em.flush();
        assertEquals(1, service.findByTag(tag.getId()).size());

        service.removeTag(t.getId(), tag.getId());
        em.flush();
        assertTrue(service.findByTag(tag.getId()).isEmpty());
    }

    @Test
    void createWithTagsSetsTags() {
        Tag a = tags.create("tagi-a", null);
        Tag b = tags.create("tagi-b", null);
        Task data = new Task();
        data.setTitle("T");

        Task t = service.create(project.getId(), data, Set.of(a.getId(), b.getId()));
        em.flush();

        assertEquals(2, service.get(t.getId()).getTags().size());
        assertEquals(1, service.findByTag(a.getId()).size());
    }

    @Test
    void updateWithRelationsChangesProjectAndSyncsTags() {
        Tag a = tags.create("tagi-a", null);
        Tag b = tags.create("tagi-b", null);
        Task data = new Task();
        data.setTitle("Vanha");
        Task t = service.create(project.getId(), data, Set.of(a.getId()));
        Project other = new Project();
        other.setName("Toinen");
        projects.create(other);
        em.flush();

        Task changes = new Task();
        changes.setTitle("Uusi");
        service.update(t.getId(), changes, other.getId(), Set.of(b.getId()));
        em.flush();
        em.clear();

        Task found = service.get(t.getId());
        assertEquals("Uusi", found.getTitle());
        assertEquals(other.getId(), found.getProject().getId());
        assertEquals(Set.of(b.getId()),
                found.getTags().stream().map(Tag::getId).collect(Collectors.toSet()));
    }

    @Test
    void searchLoadsProjectAndTagsForDisplayOutsideTransaction() {
        Tag a = tags.create("tagi-a", null);
        Task data = new Task();
        data.setTitle("Näytettävä");
        service.create(project.getId(), data, Set.of(a.getId()));
        em.flush();
        em.clear();

        Task found = service.search(TaskFilter.none()).stream()
                .filter(x -> x.getTitle().equals("Näytettävä")).findFirst().orElseThrow();

        assertTrue(jakarta.persistence.Persistence.getPersistenceUtil().isLoaded(found, "project"));
        assertTrue(jakarta.persistence.Persistence.getPersistenceUtil().isLoaded(found, "tags"));
    }

    @Test
    void deleteRemovesTask() {
        Task t = newTask("T");
        em.flush();

        service.delete(t.getId());
        em.flush();
        em.clear();

        assertThrows(EntityNotFoundException.class, () -> service.get(t.getId()));
        assertTrue(service.findByProject(project.getId()).isEmpty());
    }
}

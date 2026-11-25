package fi.app.tehtavienhallinta;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class TaskSearchTest {

    @Autowired
    TaskService service;
    @Autowired
    ProjectService projects;
    @Autowired
    TagService tags;
    @Autowired
    EntityManager em;

    // Jokaisella testillä on oma projekti, joten testidata ei häiritse tuloksia.
    Project project;
    Tag tag;
    Task a;
    Task b;
    Task c;

    @BeforeEach
    void setUp() {
        Project p = new Project();
        p.setStartDate(java.time.LocalDate.of(2026, 1, 1));
        p.setDueDate(java.time.LocalDate.of(2026, 12, 31));
        p.setName("Hakutestit");
        project = projects.create(p);
        tag = tags.create("hakutagi", "#cccccc");

        a = task("Kirjoita raportti", "Loppuraportti", LocalDate.of(2026, 3, 1), false);
        b = task("Korjaa bugi", null, LocalDate.of(2026, 3, 10), true);
        c = task("Päivitä 100% kuvaus_", "Sisältää erikoismerkkejä", LocalDate.of(2026, 3, 20), false);

        service.addTag(a.getId(), tag.getId());
        em.flush();
    }

    private Task task(String title, String description, LocalDate due, boolean done) {
        Task t = new Task();
        t.setTitle(title);
        t.setDescription(description);
        t.setDueDate(due);
        t.setDone(done);
        return service.create(project.getId(), t);
    }

    private List<Long> ids(TaskFilter f) {
        return service.search(f).stream().map(Task::getId).toList();
    }

    private TaskFilter inProject(Boolean done, LocalDate from, LocalDate to, String text, Long tagId) {
        return new TaskFilter(project.getId(), tagId, done, from, to, text);
    }

    @Test
    void emptyFilterReturnsAllTasksOfProjectSortedByDueDate() {
        assertEquals(List.of(a.getId(), b.getId(), c.getId()),
                ids(inProject(null, null, null, null, null)));
    }

    @Test
    void noneFilterReturnsEverything() {
        assertTrue(service.search(TaskFilter.none()).size() >= 3);
    }

    @Test
    void filtersByDone() {
        assertEquals(List.of(b.getId()), ids(inProject(true, null, null, null, null)));
        assertEquals(List.of(a.getId(), c.getId()), ids(inProject(false, null, null, null, null)));
    }

    @Test
    void filtersByTag() {
        assertEquals(List.of(a.getId()), ids(inProject(null, null, null, null, tag.getId())));
    }

    @Test
    void filtersByDueDateRange() {
        assertEquals(List.of(b.getId(), c.getId()),
                ids(inProject(null, LocalDate.of(2026, 3, 10), null, null, null)));
        assertEquals(List.of(a.getId(), b.getId()),
                ids(inProject(null, null, LocalDate.of(2026, 3, 10), null, null)));
        assertEquals(List.of(b.getId()),
                ids(inProject(null, LocalDate.of(2026, 3, 5), LocalDate.of(2026, 3, 15), null, null)));
    }

    @Test
    void textSearchMatchesTitleAndDescriptionIgnoringCase() {
        assertEquals(List.of(a.getId()), ids(inProject(null, null, null, "RAPORTTI", null)));
        assertEquals(List.of(c.getId()), ids(inProject(null, null, null, "erikoismerk", null)));
        assertEquals(List.of(b.getId()), ids(inProject(null, null, null, "bugi", null)));
    }

    @Test
    void textSearchTreatsWildcardsLiterally() {
        assertEquals(List.of(c.getId()), ids(inProject(null, null, null, "100%", null)));
        assertEquals(List.of(c.getId()), ids(inProject(null, null, null, "kuvaus_", null)));
        assertTrue(ids(inProject(null, null, null, "%", null)).size() == 1);
    }

    @Test
    void blankTextIsIgnored() {
        assertEquals(3, ids(inProject(null, null, null, "  ", null)).size());
    }

    @Test
    void combinesCriteria() {
        assertEquals(List.of(a.getId()),
                ids(inProject(false, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 15), "raportti", tag.getId())));
        assertTrue(ids(inProject(true, null, null, "raportti", null)).isEmpty());
    }

    // --- Edistynyt haku (CriteriaQuery) ---

    private List<Long> adv(String text, Long projectId, String projectName, Long tagId, String tagName,
                           Boolean done, Integer minPriority, LocalDate from, LocalDate to) {
        return service.advancedSearch(new TaskSearchCriteria(
                        text, projectId, projectName, tagId, tagName, done, minPriority, from, to))
                .stream().map(Task::getId).toList();
    }

    @Test
    void advancedEmptyCriteriaReturnsEverything() {
        assertTrue(service.advancedSearch(TaskSearchCriteria.none()).size() >= 3);
        assertEquals(List.of(a.getId(), b.getId(), c.getId()),
                adv(" ", project.getId(), null, null, "", null, null, null, null));
    }

    @Test
    void advancedTextMatchesTitleOrDescription() {
        assertEquals(List.of(a.getId()), adv("RAPORTTI", project.getId(), null, null, null, null, null, null, null));
        assertEquals(List.of(c.getId()), adv("erikoismerk", project.getId(), null, null, null, null, null, null, null));
        assertEquals(List.of(c.getId()), adv("100%", project.getId(), null, null, null, null, null, null, null));
    }

    @Test
    void advancedFiltersByDateRange() {
        assertEquals(List.of(b.getId()),
                adv(null, project.getId(), null, null, null, null, null,
                        LocalDate.of(2026, 3, 5), LocalDate.of(2026, 3, 15)));
        assertTrue(adv(null, project.getId(), null, null, null, null, null,
                LocalDate.of(2026, 4, 1), LocalDate.of(2026, 3, 1)).isEmpty());
    }

    @Test
    void advancedFiltersByRelatedEntityViaJoin() {
        assertEquals(List.of(a.getId()), adv(null, project.getId(), null, tag.getId(), null, null, null, null, null));
        assertEquals(List.of(a.getId()), adv(null, project.getId(), null, null, "HAKUTAG", null, null, null, null));
        assertTrue(adv(null, project.getId(), null, null, "olematon", null, null, null, null).isEmpty());
    }

    @Test
    void advancedFiltersByRelatedEntityProperty() {
        assertEquals(List.of(a.getId(), b.getId(), c.getId()),
                adv(null, null, "hakutesti", null, null, null, null, null, null).stream()
                        .filter(id -> List.of(a.getId(), b.getId(), c.getId()).contains(id)).toList());
        assertTrue(adv(null, project.getId(), "ei-tällaista", null, null, null, null, null, null).isEmpty());
    }

    @Test
    void advancedOrGroupCombinedWithAnd() {
        // (otsikko OR kuvaus sisältää "raportti") AND valmis=false AND tagi
        assertEquals(List.of(a.getId()),
                adv("raportti", project.getId(), null, tag.getId(), null, false, null, null, null));
        // sama sana, mutta valmiit: ei osumia
        assertTrue(adv("raportti", project.getId(), null, null, null, true, null, null, null).isEmpty());
    }

    @Test
    void advancedLoadsProjectAndAllTagsForDisplayOutsideTransaction() {
        Tag second = tags.create("hakutagi2", "#dddddd");
        service.addTag(a.getId(), second.getId());
        em.flush();
        em.clear();

        // Haku yhdellä tunnisteella ei saa rajata tehtävän ladattuja tunnisteita
        Task found = service.advancedSearch(new TaskSearchCriteria(
                null, null, null, tag.getId(), null, null, null, null, null)).stream()
                .filter(t -> t.getId().equals(a.getId())).findFirst().orElseThrow();

        assertTrue(org.hibernate.Hibernate.isInitialized(found.getProject()));
        assertTrue(org.hibernate.Hibernate.isInitialized(found.getTags()));
        assertEquals(2, found.getTags().size());
    }

    @Test
    void advancedDoesNotReturnDuplicatesWhenManyTagsMatch() {
        Tag second = tags.create("hakutagi2", "#dddddd");
        service.addTag(a.getId(), second.getId());
        em.flush();

        assertEquals(List.of(a.getId()),
                adv(null, project.getId(), null, null, "hakutagi", null, null, null, null));
    }

    @Test
    void findOverdueReturnsOnlyUnfinishedPastDue() {
        List<Long> overdue = service.findOverdue(LocalDate.of(2026, 3, 15)).stream()
                .filter(t -> t.getProject().getId().equals(project.getId()))
                .map(Task::getId).toList();

        assertEquals(List.of(a.getId()), overdue);
    }
}

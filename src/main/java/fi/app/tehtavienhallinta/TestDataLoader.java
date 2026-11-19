package fi.app.tehtavienhallinta;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

/**
 * Lataa kehityskäyttöön testidatan, jos tietokanta on tyhjä.
 */
@Configuration
public class TestDataLoader {

    @Bean
    CommandLineRunner loadTestData(AppUserRepository users,
                                   ProjectRepository projects,
                                   TaskRepository tasks,
                                   TagRepository tags) {
        return args -> {
            if (projects.count() > 0 || users.count() > 0 || tags.count() > 0) {
                return;
            }

            users.save(new AppUser("ansu", "ansu@example.com"));
            users.save(new AppUser("matti", "matti@example.com"));

            Tag urgent = tag(tags, "kiireellinen", "#e53935");
            Tag bug = tag(tags, "bugi", "#fb8c00");
            Tag feature = tag(tags, "ominaisuus", "#43a047");
            Tag docs = tag(tags, "dokumentointi", "#1e88e5");

            Project web = project("Verkkosivu-uudistus", "Yrityksen verkkosivujen uudistus",
                    LocalDate.now().minusDays(14), LocalDate.now().plusDays(30));
            web.addTask(task("Suunnittele etusivu", "Wireframet ja värimaailma",
                    LocalDate.now().plusDays(5), false, feature));
            web.addTask(task("Korjaa yhteydenottolomake", "Lomake ei lähetä sähköpostia",
                    LocalDate.now().plusDays(1), false, bug, urgent));
            web.addTask(task("Kirjoita sisällöt", null,
                    LocalDate.now().plusDays(20), true, docs));
            projects.save(web);

            Project app = project("Mobiilisovellus", "Tehtävienhallinnan mobiiliversio",
                    LocalDate.now(), LocalDate.now().plusDays(90));
            app.addTask(task("Valitse teknologia", "React Native vai Flutter",
                    LocalDate.now().plusDays(7), false, feature));
            app.addTask(task("Kirjoita käyttöohje", null, null, false, docs));
            projects.save(app);

            Project old = project("Vanha projekti", "Arkistoitu esimerkkiprojekti",
                    LocalDate.now().minusDays(120), LocalDate.now().minusDays(30));
            old.setArchived(true);
            old.addTask(task("Viimeistele raportti", null,
                    LocalDate.now().minusDays(35), true));
            projects.save(old);
        };
    }

    private static Tag tag(TagRepository repo, String name, String color) {
        Tag t = new Tag();
        t.setName(name);
        t.setColor(color);
        return repo.save(t);
    }

    private static Project project(String name, String description, LocalDate start, LocalDate due) {
        Project p = new Project();
        p.setName(name);
        p.setDescription(description);
        p.setStartDate(start);
        p.setDueDate(due);
        return p;
    }

    private static Task task(String title, String description, LocalDate due, boolean done, Tag... taskTags) {
        Task t = new Task();
        t.setTitle(title);
        t.setDescription(description);
        t.setDueDate(due);
        t.setDone(done);
        for (Tag tag : taskTags) {
            t.addTag(tag);
        }
        return t;
    }
}

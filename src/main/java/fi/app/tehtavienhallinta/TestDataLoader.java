package fi.app.tehtavienhallinta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Lataa kehityskäyttöön testidatan, jos tietokanta on tyhjä.
 */
@Configuration
public class TestDataLoader {

    private static final Logger log = LoggerFactory.getLogger(TestDataLoader.class);

    @Bean
    CommandLineRunner loadTestData(@Value("${APP_DEV_PASSWORD:}") String devPassword,
                                   AppUserRepository users,
                                   PasswordEncoder passwordEncoder,
                                   ProjectRepository projects,
                                   TaskRepository tasks,
                                   TagRepository tags) {
        return args -> {
            if (projects.count() > 0 || users.count() > 0 || tags.count() > 0) {
                return;
            }

            // Salasanaa ei kirjoiteta koodiin: se tulee ympäristömuuttujasta APP_DEV_PASSWORD
            // tai, jos sitä ei ole asetettu, generoidaan satunnaisesti ja tulostetaan lokiin.
            String rawPassword = devPassword;
            if (rawPassword == null || rawPassword.isBlank()) {
                rawPassword = UUID.randomUUID().toString();
                log.warn("Testikäyttäjien (ansu, matti) generoitu salasana: {}", rawPassword);
            }
            String hash = passwordEncoder.encode(rawPassword);
            users.save(new AppUser("ansu", "ansu@example.com", hash, Role.ADMIN));
            users.save(new AppUser("matti", "matti@example.com", hash, Role.USER));

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

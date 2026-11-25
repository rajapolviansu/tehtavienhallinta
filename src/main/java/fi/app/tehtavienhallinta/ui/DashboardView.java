package fi.app.tehtavienhallinta.ui;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import fi.app.tehtavienhallinta.Project;
import fi.app.tehtavienhallinta.ProjectService;
import fi.app.tehtavienhallinta.Task;
import fi.app.tehtavienhallinta.TaskService;
import jakarta.annotation.security.PermitAll;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

/**
 * Kojelauta: tilastokortit (kortti-/ruudukkoasettelu) ja lähimmät erääntyvät tehtävät.
 */
@Route(value = "kojelauta", layout = MainLayout.class)
@PageTitle("Kojelauta | Tehtävienhallinta")
@PermitAll
@StyleSheet("dashboard-view.css")
public class DashboardView extends VerticalLayout {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d.M.yyyy");

    public DashboardView(TaskService tasks, ProjectService projects) {
        addClassName("dashboard-view");
        setPadding(true);

        List<Task> all = tasks.findAll();
        long open = all.stream().filter(t -> !t.isDone()).count();
        long done = all.size() - open;
        long overdue = tasks.findOverdue(LocalDate.now()).size();
        List<Project> allProjects = projects.findAll();

        add(new H2("Kojelauta"));

        FlexLayout cards = new FlexLayout(
                statCard("Avoimet tehtävät", open, "stat-open"),
                statCard("Valmiit tehtävät", done, "stat-done"),
                statCard("Myöhässä", overdue, "stat-overdue"),
                statCard("Projektit", allProjects.size(), "stat-projects"));
        cards.addClassName("stat-cards");
        add(cards);

        add(new H3("Seuraavaksi erääntyvät"));
        Div upcoming = new Div();
        upcoming.addClassName("upcoming-list");
        all.stream()
                .filter(t -> !t.isDone() && t.getDueDate() != null)
                .sorted(Comparator.comparing(Task::getDueDate))
                .limit(5)
                .forEach(t -> upcoming.add(upcomingRow(t)));
        if (upcoming.getChildren().findAny().isEmpty()) {
            upcoming.add(new Span("Ei erääntyviä tehtäviä."));
        }
        add(upcoming);
    }

    private Div statCard(String label, long value, String variant) {
        Span number = new Span(String.valueOf(value));
        number.addClassName("stat-number");
        Span text = new Span(label);
        text.addClassName("stat-label");
        Div card = new Div(number, text);
        card.addClassNames("stat-card", variant, "zoom-card");
        return card;
    }

    private Div upcomingRow(Task t) {
        Span title = new Span(t.getTitle());
        title.addClassName("upcoming-title");
        Span due = new Span(DATE.format(t.getDueDate()));
        due.addClassName("upcoming-due");
        if (t.getDueDate().isBefore(LocalDate.now())) {
            due.addClassName("overdue");
        }
        Div row = new Div(title, due);
        row.addClassName("upcoming-row");
        return row;
    }
}

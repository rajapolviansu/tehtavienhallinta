package fi.app.tehtavienhallinta.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import fi.app.tehtavienhallinta.ProjectService;
import fi.app.tehtavienhallinta.Task;
import fi.app.tehtavienhallinta.TaskService;
import jakarta.annotation.security.PermitAll;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

/**
 * Tehtävätaulu (Kanban): kolme saraketta vierekkäin. Kortin painikkeella tehtävä siirtyy
 * valmiiksi tai takaisin avoimeksi.
 */
@Route(value = "taulu", layout = MainLayout.class)
@PageTitle("Taulu | Tehtävienhallinta")
@PermitAll
@StyleSheet("board-view.css")
public class BoardView extends VerticalLayout {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d.M.yyyy");

    private final TaskService tasks;
    private final FlexLayout columns = new FlexLayout();

    public BoardView(TaskService tasks, ProjectService projects) {
        this.tasks = tasks;
        addClassName("board-view");
        setSizeFull();
        columns.addClassName("board-columns");
        add(new H2("Tehtävätaulu"), columns);
        setFlexGrow(1, columns);
        refresh();
    }

    private void refresh() {
        columns.removeAll();
        LocalDate today = LocalDate.now();
        List<Task> all = tasks.findAll();
        columns.add(
                column("Tekemättä", "col-todo", all,
                        t -> !t.isDone() && (t.getDueDate() == null || !t.getDueDate().isBefore(today))),
                column("Myöhässä", "col-overdue", all,
                        t -> !t.isDone() && t.getDueDate() != null && t.getDueDate().isBefore(today)),
                column("Valmis", "col-done", all, Task::isDone));
    }

    private Div column(String title, String variant, List<Task> all, Predicate<Task> filter) {
        List<Task> items = all.stream().filter(filter)
                .sorted(Comparator.comparing(Task::getDueDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
        Span count = new Span(String.valueOf(items.size()));
        count.addClassName("column-count");
        Div header = new Div(new H3(title), count);
        header.addClassName("column-header");

        Div column = new Div(header);
        column.addClassNames("board-column", variant);
        items.forEach(t -> column.add(card(t)));
        return column;
    }

    private Div card(Task t) {
        Span title = new Span(t.getTitle());
        title.addClassName("card-title");
        Div card = new Div(title);
        card.addClassNames("task-card", "zoom-card");

        if (t.getProject() != null) {
            Span project = new Span(t.getProject().getName());
            project.addClassName("card-project");
            card.add(project);
        }
        if (t.getDueDate() != null) {
            Span due = new Span("Eräpäivä " + DATE.format(t.getDueDate()));
            due.addClassName("card-due");
            card.add(due);
        }

        Button toggle = new Button(t.isDone() ? "Avaa uudelleen" : "Merkitse valmiiksi", e -> {
            tasks.setDone(t.getId(), !t.isDone());
            refresh();
        });
        toggle.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        card.add(toggle);
        return card;
    }
}

package fi.app.tehtavienhallinta.ui;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import fi.app.tehtavienhallinta.Project;
import fi.app.tehtavienhallinta.ProjectService;
import fi.app.tehtavienhallinta.Task;
import fi.app.tehtavienhallinta.TaskService;
import jakarta.annotation.security.PermitAll;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Projektinäkymä (master-detail): vasemmalla projektilista, oikealla valitun projektin tiedot
 * ja tehtävät. Jakajaa voi vetää.
 */
@Route(value = "projektinakyma", layout = MainLayout.class)
@PageTitle("Projektinäkymä | Tehtävienhallinta")
@PermitAll
@StyleSheet("project-detail.css")
public class ProjectDetailView extends VerticalLayout {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d.M.yyyy");

    private final TaskService tasks;
    private final Grid<Project> projectGrid = new Grid<>(Project.class, false);
    private final VerticalLayout detail = new VerticalLayout();

    public ProjectDetailView(TaskService tasks, ProjectService projects) {
        this.tasks = tasks;
        addClassName("project-detail-view");
        setSizeFull();
        setPadding(false);

        projectGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        projectGrid.addColumn(Project::getName).setHeader("Projektit");
        projectGrid.setItems(projects.findAll());
        projectGrid.setSizeFull();
        projectGrid.asSingleSelect().addValueChangeListener(e -> showDetails(e.getValue()));

        detail.addClassName("project-detail-panel");
        showDetails(null);

        SplitLayout split = new SplitLayout(projectGrid, detail);
        split.setSizeFull();
        split.setSplitterPosition(30);
        add(split);
        setFlexGrow(1, split);
    }

    private void showDetails(Project p) {
        detail.removeAll();
        if (p == null) {
            detail.add(new Span("Valitse projekti vasemmalta."));
            return;
        }
        detail.add(new H2(p.getName()));
        if (p.getDescription() != null && !p.getDescription().isBlank()) {
            detail.add(new Paragraph(p.getDescription()));
        }
        Div dates = new Div(new Span("Alkupäivä: " + (p.getStartDate() == null ? "-" : DATE.format(p.getStartDate()))),
                new Span("Eräpäivä: " + (p.getDueDate() == null ? "-" : DATE.format(p.getDueDate()))));
        dates.addClassName("project-dates");
        detail.add(dates);

        List<Task> projectTasks = tasks.findByProject(p.getId());
        detail.add(new H3("Tehtävät (" + projectTasks.size() + ")"));
        Grid<Task> taskGrid = new Grid<>(Task.class, false);
        taskGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        taskGrid.addColumn(Task::getTitle).setHeader("Otsikko").setFlexGrow(3);
        taskGrid.addColumn(t -> t.getDueDate() == null ? "" : DATE.format(t.getDueDate())).setHeader("Eräpäivä");
        taskGrid.addColumn(t -> t.isDone() ? "Valmis" : "Kesken").setHeader("Tila");
        taskGrid.setItems(projectTasks);
        detail.add(taskGrid);
    }
}

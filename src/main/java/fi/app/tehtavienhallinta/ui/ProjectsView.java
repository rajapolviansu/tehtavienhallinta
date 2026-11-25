package fi.app.tehtavienhallinta.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import fi.app.tehtavienhallinta.Project;
import fi.app.tehtavienhallinta.ProjectService;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;

/**
 * Projektilista: lisäys, muokkaus ja poisto (poisto vahvistetaan, ja se poistaa myös projektin tehtävät).
 */
@Route(value = "projektit", layout = MainLayout.class)
@PageTitle("Projektit | Tehtävienhallinta")
@PermitAll
public class ProjectsView extends VerticalLayout {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d.M.yyyy");

    private final ProjectService projects;
    private final Grid<Project> grid = new Grid<>(Project.class, false);

    public ProjectsView(ProjectService projects) {
        this.projects = projects;

        setSizeFull();
        setPadding(false);

        Button add = new Button("Uusi projekti", VaadinIcon.PLUS.create(), e -> openDialog(null));
        add.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        configureGrid();
        add(add, grid);
        setFlexGrow(1, grid);
        refresh();
    }

    private void configureGrid() {
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.setSizeFull();

        grid.addColumn(Project::getName).setHeader("Nimi").setFlexGrow(2)
                .setComparator(Comparator.comparing(Project::getName, String.CASE_INSENSITIVE_ORDER));
        grid.addColumn(Project::getDescription).setHeader("Kuvaus").setFlexGrow(3);
        grid.addColumn(p -> p.getStartDate() == null ? "" : DATE.format(p.getStartDate()))
                .setHeader("Alkupäivä").setAutoWidth(true).setFlexGrow(0)
                .setComparator(Comparator.comparing(Project::getStartDate,
                        Comparator.nullsLast(Comparator.naturalOrder())));
        grid.addColumn(p -> p.getDueDate() == null ? "" : DATE.format(p.getDueDate()))
                .setHeader("Eräpäivä").setAutoWidth(true).setFlexGrow(0)
                .setComparator(Comparator.comparing(Project::getDueDate,
                        Comparator.nullsLast(Comparator.naturalOrder())));
        grid.addColumn(p -> p.getBudget() == null ? "" : p.getBudget().toPlainString() + " €")
                .setHeader("Budjetti").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(new ComponentRenderer<>(p -> {
            Checkbox box = new Checkbox(p.isArchived());
            box.setReadOnly(true);
            box.setAriaLabel("Arkistoitu");
            return box;
        })).setHeader("Arkistoitu").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(new ComponentRenderer<>(p ->
                Ui.rowActions(() -> openDialog(p), () -> confirmDelete(p), true)))
                .setHeader("").setAutoWidth(true).setFlexGrow(0);
    }

    void refresh() {
        grid.setItems(projects.findAll());
    }

    private void openDialog(Project project) {
        new ProjectDialog(projects, project, this::refresh).open();
    }

    private void confirmDelete(Project project) {
        Ui.confirmDelete("Poistetaanko projekti?",
                "Projekti \"" + project.getName() + "\" ja kaikki sen tehtävät poistetaan pysyvästi.",
                () -> Ui.runDelete(() -> projects.delete(project.getId()), "Projekti poistettu", this::refresh));
    }
}

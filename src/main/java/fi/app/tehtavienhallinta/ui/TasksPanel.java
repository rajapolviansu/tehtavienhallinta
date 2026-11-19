package fi.app.tehtavienhallinta.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import fi.app.tehtavienhallinta.ProjectService;
import fi.app.tehtavienhallinta.Tag;
import fi.app.tehtavienhallinta.TagService;
import fi.app.tehtavienhallinta.Task;
import fi.app.tehtavienhallinta.TaskFilter;
import fi.app.tehtavienhallinta.TaskService;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * Tehtävälista: haku, lisäys, muokkaus ja poisto (poisto vahvistetaan).
 */
@SpringComponent
@UIScope
public class TasksPanel extends VerticalLayout {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d.M.yyyy");

    private final TaskService tasks;
    private final ProjectService projects;
    private final TagService tags;

    private final Grid<Task> grid = new Grid<>(Task.class, false);
    private final TextField search = new TextField();
    private final Checkbox onlyOpen = new Checkbox("Vain keskeneräiset");

    public TasksPanel(TaskService tasks, ProjectService projects, TagService tags) {
        this.tasks = tasks;
        this.projects = projects;
        this.tags = tags;

        setSizeFull();
        setPadding(false);

        search.setPlaceholder("Hae otsikosta tai kuvauksesta");
        search.setClearButtonVisible(true);
        search.setValueChangeMode(ValueChangeMode.LAZY);
        search.setWidth("20em");
        search.addValueChangeListener(e -> refresh());
        onlyOpen.addValueChangeListener(e -> refresh());

        Button add = new Button("Uusi tehtävä", VaadinIcon.PLUS.create(), e -> openDialog(null));
        add.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout toolbar = new HorizontalLayout(search, onlyOpen, add);
        toolbar.setWidthFull();
        toolbar.setAlignItems(FlexComponent.Alignment.CENTER);

        configureGrid();
        add(toolbar, grid);
        setFlexGrow(1, grid);
        refresh();
    }

    private void configureGrid() {
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.setSizeFull();

        grid.addColumn(new ComponentRenderer<>(task -> {
            Checkbox box = new Checkbox(task.isDone());
            box.setAriaLabel("Valmis");
            box.addValueChangeListener(e -> {
                tasks.setDone(task.getId(), e.getValue());
                if (onlyOpen.getValue()) {
                    refresh();
                }
            });
            return box;
        })).setHeader("Valmis").setAutoWidth(true).setFlexGrow(0);

        grid.addColumn(Task::getTitle).setHeader("Otsikko").setFlexGrow(2)
                .setComparator(Comparator.comparing(Task::getTitle, String.CASE_INSENSITIVE_ORDER));
        grid.addColumn(t -> t.getProject() == null ? "" : t.getProject().getName())
                .setHeader("Projekti").setFlexGrow(1);
        grid.addColumn(t -> t.getTags().stream().map(Tag::getName).sorted()
                        .collect(Collectors.joining(", ")))
                .setHeader("Tunnisteet").setFlexGrow(1);
        grid.addColumn(t -> t.getDueDate() == null ? "" : DATE.format(t.getDueDate()))
                .setHeader("Eräpäivä").setAutoWidth(true).setFlexGrow(0)
                .setComparator(Comparator.comparing(Task::getDueDate,
                        Comparator.nullsLast(Comparator.naturalOrder())));

        grid.addColumn(new ComponentRenderer<>(task -> {
            Button edit = new Button(VaadinIcon.EDIT.create(), e -> openDialog(task));
            edit.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            edit.setAriaLabel("Muokkaa");
            Button delete = new Button(VaadinIcon.TRASH.create(), e -> confirmDelete(task));
            delete.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            delete.setAriaLabel("Poista");
            return new HorizontalLayout(edit, delete);
        })).setHeader("").setAutoWidth(true).setFlexGrow(0);
    }

    private void refresh() {
        String text = search.getValue() == null || search.getValue().isBlank()
                ? null : search.getValue().trim();
        Boolean done = onlyOpen.getValue() ? Boolean.FALSE : null;
        grid.setItems(tasks.search(new TaskFilter(null, null, done, null, null, text)));
    }

    private void openDialog(Task task) {
        new TaskDialog(tasks, projects, tags, task, this::refresh).open();
    }

    private void confirmDelete(Task task) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Poistetaanko tehtävä?");
        dialog.setText("Tehtävä \"" + task.getTitle() + "\" poistetaan pysyvästi.");
        dialog.setCancelable(true);
        dialog.setCancelText("Peruuta");
        dialog.setConfirmText("Poista");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(e -> {
            try {
                tasks.delete(task.getId());
                Notification.show("Tehtävä poistettu").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (RuntimeException ex) {
                Notification.show("Poisto epäonnistui: " + ex.getMessage())
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
            refresh();
        });
        dialog.open();
    }
}

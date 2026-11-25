package fi.app.tehtavienhallinta.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import fi.app.tehtavienhallinta.Project;
import fi.app.tehtavienhallinta.ProjectService;
import fi.app.tehtavienhallinta.Tag;
import fi.app.tehtavienhallinta.TagService;
import fi.app.tehtavienhallinta.Task;
import fi.app.tehtavienhallinta.TaskSearchCriteria;
import fi.app.tehtavienhallinta.TaskService;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * Edistynyt hakunäkymä: useita ehtoja yhdistetään (AND), tekstihaku on otsikko OR kuvaus ja
 * hakua voi rajata projektin ja tunnisteen nimellä (JOIN). Tyhjiä kenttiä ei oteta huomioon.
 */
@SpringComponent
@UIScope
public class SearchPanel extends VerticalLayout {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d.M.yyyy");

    private final TaskService tasks;
    private final ProjectService projects;
    private final TagService tags;

    private final TextField text = new TextField("Hakusana (otsikko tai kuvaus)");
    private final ComboBox<Project> project = new ComboBox<>("Projekti");
    private final TextField projectName = new TextField("Projektin nimi sisältää");
    private final ComboBox<Tag> tag = new ComboBox<>("Tunniste");
    private final TextField tagName = new TextField("Tunnisteen nimi sisältää");
    private final ComboBox<String> status = new ComboBox<>("Tila");
    private final IntegerField minPriority = new IntegerField("Tärkeys vähintään");
    private final DatePicker dueFrom = new FinnishDatePicker("Eräpäivä alkaen");
    private final DatePicker dueTo = new FinnishDatePicker("Eräpäivä asti");

    private final Grid<Task> grid = new Grid<>(Task.class, false);
    private final Span resultCount = new Span();

    public SearchPanel(TaskService tasks, ProjectService projects, TagService tags) {
        this.tasks = tasks;
        this.projects = projects;
        this.tags = tags;

        setSizeFull();
        setPadding(false);

        project.setItemLabelGenerator(Project::getName);
        project.setClearButtonVisible(true);
        tag.setItemLabelGenerator(Tag::getName);
        tag.setClearButtonVisible(true);
        status.setItems("Keskeneräiset", "Valmiit");
        status.setClearButtonVisible(true);
        minPriority.setMin(1);
        minPriority.setMax(5);
        minPriority.setStepButtonsVisible(true);
        for (TextField f : new TextField[] {text, projectName, tagName}) {
            f.setClearButtonVisible(true);
        }
        dueFrom.setClearButtonVisible(true);
        dueTo.setClearButtonVisible(true);

        FormLayout form = new FormLayout(text, project, projectName, tag, tagName, status,
                minPriority, dueFrom, dueTo);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("40em", 3));

        Button searchButton = new Button("Hae", VaadinIcon.SEARCH.create(), e -> search());
        searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        searchButton.addClickShortcut(com.vaadin.flow.component.Key.ENTER);
        Button clear = new Button("Tyhjennä", e -> clear());
        HorizontalLayout actions = new HorizontalLayout(searchButton, clear, resultCount);
        actions.setAlignItems(Alignment.CENTER);

        configureGrid();
        add(form, actions, grid);
        setFlexGrow(1, grid);
        reloadOptions();
        search();
    }

    private void configureGrid() {
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.setSizeFull();
        grid.addColumn(t -> t.isDone() ? "Kyllä" : "Ei").setHeader("Valmis")
                .setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(Task::getTitle).setHeader("Otsikko").setFlexGrow(2)
                .setComparator(Comparator.comparing(Task::getTitle, String.CASE_INSENSITIVE_ORDER));
        grid.addColumn(t -> t.getProject() == null ? "" : t.getProject().getName())
                .setHeader("Projekti").setFlexGrow(1);
        grid.addColumn(t -> t.getTags().stream().map(Tag::getName).sorted()
                        .collect(Collectors.joining(", ")))
                .setHeader("Tunnisteet").setFlexGrow(1);
        grid.addColumn(Task::getPriority).setHeader("Tärkeys").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(t -> t.getDueDate() == null ? "" : DATE.format(t.getDueDate()))
                .setHeader("Eräpäivä").setAutoWidth(true).setFlexGrow(0)
                .setComparator(Comparator.comparing(Task::getDueDate,
                        Comparator.nullsLast(Comparator.naturalOrder())));
    }

    /** Päivittää valintalistat (projektit ja tunnisteet voivat muuttua muissa välilehdissä). */
    public void reloadOptions() {
        Project selectedProject = project.getValue();
        Tag selectedTag = tag.getValue();
        project.setItems(projects.findAll());
        tag.setItems(tags.findAll());
        project.setValue(selectedProject == null ? null
                : projects.findAll().stream().filter(p -> p.getId().equals(selectedProject.getId()))
                        .findFirst().orElse(null));
        tag.setValue(selectedTag == null ? null
                : tags.findAll().stream().filter(t -> t.getId().equals(selectedTag.getId()))
                        .findFirst().orElse(null));
    }

    private void search() {
        if (dueFrom.getValue() != null && dueTo.getValue() != null
                && dueFrom.getValue().isAfter(dueTo.getValue())) {
            dueTo.setInvalid(true);
            dueTo.setErrorMessage("Loppupäivä ei voi olla alkupäivää ennen");
            Notification.show("Tarkista päivämäärävälin ehdot");
            return;
        }
        dueTo.setInvalid(false);

        Boolean done = status.getValue() == null ? null : "Valmiit".equals(status.getValue());
        var results = tasks.advancedSearch(new TaskSearchCriteria(
                text.getValue(),
                project.getValue() == null ? null : project.getValue().getId(),
                projectName.getValue(),
                tag.getValue() == null ? null : tag.getValue().getId(),
                tagName.getValue(),
                done,
                minPriority.getValue(),
                dueFrom.getValue(),
                dueTo.getValue()));
        grid.setItems(results);
        resultCount.setText(results.size() + " tehtävää");
    }

    private void clear() {
        text.clear();
        project.clear();
        projectName.clear();
        tag.clear();
        tagName.clear();
        status.clear();
        minPriority.clear();
        dueFrom.clear();
        dueTo.clear();
        search();
    }
}

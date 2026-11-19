package fi.app.tehtavienhallinta.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import fi.app.tehtavienhallinta.Project;
import fi.app.tehtavienhallinta.ProjectService;
import fi.app.tehtavienhallinta.Tag;
import fi.app.tehtavienhallinta.TagService;
import fi.app.tehtavienhallinta.Task;
import fi.app.tehtavienhallinta.TaskService;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Lomake tehtävän lisäämiseen ja muokkaamiseen. Lomake muokkaa tehtävästä tehtyä kopiota,
 * ja tiedot tallennetaan palvelun kautta vasta kun kaikki kentät ovat kelvollisia.
 */
class TaskDialog extends Dialog {

    private final TaskService tasks;
    private final Task existing;
    private final Runnable onSaved;

    private final Task working = new Task();
    private final Binder<Task> binder = new BeanValidationBinder<>(Task.class);

    TaskDialog(TaskService tasks, ProjectService projects, TagService tags,
               Task existing, Runnable onSaved) {
        this.tasks = tasks;
        this.existing = existing;
        this.onSaved = onSaved;

        setHeaderTitle(existing == null ? "Uusi tehtävä" : "Muokkaa tehtävää");
        setWidth("32em");

        TextField title = new TextField("Otsikko");
        title.setMaxLength(200);
        TextArea description = new TextArea("Kuvaus");
        description.setMaxLength(2000);
        DatePicker dueDate = new DatePicker("Eräpäivä");
        Checkbox done = new Checkbox("Valmis");

        ComboBox<Project> project = new ComboBox<>("Projekti");
        project.setItems(projects.findAll());
        project.setItemLabelGenerator(Project::getName);

        MultiSelectComboBox<Tag> tagField = new MultiSelectComboBox<>("Tunnisteet");
        tagField.setItems(tags.findAll());
        tagField.setItemLabelGenerator(Tag::getName);

        binder.forField(title).asRequired("Otsikko on pakollinen").bind("title");
        binder.forField(description).bind("description");
        binder.forField(dueDate).bind("dueDate");
        binder.forField(done).bind("done");
        binder.forField(project).asRequired("Valitse projekti").bind(Task::getProject, Task::setProject);
        binder.forField(tagField).bind(Task::getTags, (t, selected) -> {
            t.getTags().clear();
            t.getTags().addAll(selected);
        });

        if (existing != null) {
            working.setTitle(existing.getTitle());
            working.setDescription(existing.getDescription());
            working.setDueDate(existing.getDueDate());
            working.setDone(existing.isDone());
            working.setProject(existing.getProject());
            working.getTags().addAll(existing.getTags());
        }
        binder.readBean(working);

        FormLayout form = new FormLayout(title, description, dueDate, done, project, tagField);
        form.setColspan(title, 2);
        form.setColspan(description, 2);
        form.setColspan(tagField, 2);
        add(form);

        Button save = new Button("Tallenna", e -> save());
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancel = new Button("Peruuta", e -> close());
        getFooter().add(cancel, save);
    }

    private void save() {
        if (!binder.writeBeanIfValid(working)) {
            return;
        }
        Long projectId = working.getProject().getId();
        Set<Long> tagIds = working.getTags().stream().map(Tag::getId).collect(Collectors.toSet());
        Task data = scalarCopy(working);
        try {
            if (existing == null) {
                tasks.create(projectId, data, tagIds);
            } else {
                tasks.update(existing.getId(), data, projectId, tagIds);
            }
        } catch (RuntimeException ex) {
            Notification n = Notification.show("Tallennus epäonnistui: " + ex.getMessage());
            n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }
        Notification.show("Tehtävä tallennettu").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        close();
        onSaved.run();
    }

    /** Kopio vain tehtävän omista kentistä, jotta irralliset projekti- ja tunnisteoliot eivät päädy palveluun. */
    private static Task scalarCopy(Task source) {
        Task copy = new Task();
        copy.setTitle(source.getTitle());
        copy.setDescription(source.getDescription());
        copy.setDueDate(source.getDueDate());
        copy.setDone(source.isDone());
        return copy;
    }
}

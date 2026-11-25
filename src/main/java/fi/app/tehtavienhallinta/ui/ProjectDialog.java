package fi.app.tehtavienhallinta.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import fi.app.tehtavienhallinta.Project;
import fi.app.tehtavienhallinta.ProjectService;

/**
 * Lomake projektin lisäämiseen ja muokkaamiseen.
 */
class ProjectDialog extends Dialog {

    private final ProjectService projects;
    private final Project existing;
    private final Runnable onSaved;

    private final Project working = new Project();
    private final Binder<Project> binder = new BeanValidationBinder<>(Project.class);

    ProjectDialog(ProjectService projects, Project existing, Runnable onSaved) {
        this.projects = projects;
        this.existing = existing;
        this.onSaved = onSaved;

        setHeaderTitle(existing == null ? "Uusi projekti" : "Muokkaa projektia");
        setWidth("32em");

        TextField name = new TextField("Nimi");
        name.setMaxLength(200);
        TextArea description = new TextArea("Kuvaus");
        description.setMaxLength(2000);
        DatePicker start = new FinnishDatePicker("Alkupäivä");
        DatePicker due = new FinnishDatePicker("Eräpäivä");
        BigDecimalField budget = new BigDecimalField("Budjetti (€)");
        budget.setHelperText("Valinnainen, ei negatiivinen");
        Checkbox archived = new Checkbox("Arkistoitu");

        binder.forField(name).asRequired("Nimi on pakollinen")
                .withValidator(n -> n.trim().length() >= 3, "Nimen tulee olla vähintään 3 merkkiä")
                .bind("name");
        binder.forField(description).bind("description");
        binder.forField(start).asRequired("Alkupäivä on pakollinen").bind("startDate");
        binder.forField(due).asRequired("Eräpäivä on pakollinen").bind("dueDate");
        binder.forField(budget)
                .withValidator(b -> b == null || b.signum() >= 0, "Budjetti ei voi olla negatiivinen")
                .withValidator(b -> b == null || b.scale() <= 2, "Enintään kaksi desimaalia")
                .bind("budget");
        binder.forField(archived).bind("archived");
        binder.withValidator(p -> p.getStartDate() == null || p.getDueDate() == null
                        || !p.getDueDate().isBefore(p.getStartDate()),
                "Eräpäivä ei voi olla ennen alkupäivää");

        if (existing != null) {
            working.setName(existing.getName());
            working.setDescription(existing.getDescription());
            working.setStartDate(existing.getStartDate());
            working.setDueDate(existing.getDueDate());
            working.setBudget(existing.getBudget());
            working.setArchived(existing.isArchived());
        }
        binder.readBean(working);

        FormLayout form = new FormLayout(name, description, start, due, budget, archived);
        form.setColspan(name, 2);
        form.setColspan(description, 2);
        add(form);

        Button save = new Button("Tallenna", e -> save());
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getFooter().add(new Button("Peruuta", e -> close()), save);
    }

    private void save() {
        if (!binder.writeBeanIfValid(working)) {
            return;
        }
        try {
            if (existing == null) {
                projects.create(working);
            } else {
                projects.update(existing.getId(), working);
            }
        } catch (RuntimeException ex) {
            Ui.error("Tallennus epäonnistui: " + ex.getMessage());
            return;
        }
        Ui.success("Projekti tallennettu");
        close();
        onSaved.run();
    }
}

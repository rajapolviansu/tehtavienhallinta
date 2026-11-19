package fi.app.tehtavienhallinta.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import fi.app.tehtavienhallinta.Tag;
import fi.app.tehtavienhallinta.TagService;

/**
 * Lomake tunnisteen lisäämiseen ja muokkaamiseen.
 */
class TagDialog extends Dialog {

    static final String COLOR_PATTERN = "^#[0-9a-fA-F]{6}$";

    private final TagService tags;
    private final Tag existing;
    private final Runnable onSaved;

    private final Tag working = new Tag();
    private final Binder<Tag> binder = new BeanValidationBinder<>(Tag.class);

    TagDialog(TagService tags, Tag existing, Runnable onSaved) {
        this.tags = tags;
        this.existing = existing;
        this.onSaved = onSaved;

        setHeaderTitle(existing == null ? "Uusi tunniste" : "Muokkaa tunnistetta");
        setWidth("26em");

        TextField name = new TextField("Nimi");
        name.setMaxLength(50);
        TextField color = new TextField("Väri");
        color.setPlaceholder("#e53935");
        color.setHelperText("Valinnainen, muodossa #rrggbb");
        color.setMaxLength(7);

        binder.forField(name).asRequired("Nimi on pakollinen").bind("name");
        binder.forField(color)
                .withConverter(s -> s == null || s.isBlank() ? null : s.trim(), s -> s == null ? "" : s)
                .withValidator(c -> c == null || c.matches(COLOR_PATTERN),
                        "Anna väri muodossa #rrggbb, esim. #e53935")
                .bind("color");

        if (existing != null) {
            working.setName(existing.getName());
            working.setColor(existing.getColor());
        }
        binder.readBean(working);

        add(new FormLayout(name, color));

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
                tags.create(working.getName(), working.getColor());
            } else {
                tags.update(existing.getId(), working.getName(), working.getColor());
            }
        } catch (RuntimeException ex) {
            Ui.error("Tallennus epäonnistui: " + ex.getMessage());
            return;
        }
        Ui.success("Tunniste tallennettu");
        close();
        onSaved.run();
    }
}

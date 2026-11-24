package fi.app.tehtavienhallinta.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.data.validator.StringLengthValidator;
import fi.app.tehtavienhallinta.AppUser;
import fi.app.tehtavienhallinta.AppUserService;
import fi.app.tehtavienhallinta.Role;

/**
 * Lomake käyttäjän lisäämiseen ja muokkaamiseen. Salasana asetetaan vain uutta käyttäjää luotaessa.
 */
class UserDialog extends Dialog {

    /** Lomakkeen kenttien arvot; salasana ei ole AppUserin ominaisuus, joten se on tässä erikseen. */
    private static final class Form {
        String username;
        String email;
        String password;
        Role role = Role.USER;
    }

    private final AppUserService users;
    private final AppUser existing;
    private final Runnable onSaved;

    private final Form form = new Form();
    private final Binder<Form> binder = new Binder<>();

    UserDialog(AppUserService users, AppUser existing, boolean editingSelf, Runnable onSaved) {
        this.users = users;
        this.existing = existing;
        this.onSaved = onSaved;

        setHeaderTitle(existing == null ? "Uusi käyttäjä" : "Muokkaa käyttäjää");
        setWidth("28em");

        TextField username = new TextField("Käyttäjätunnus");
        username.setMaxLength(50);
        EmailField email = new EmailField("Sähköposti");
        ComboBox<Role> role = new ComboBox<>("Rooli");
        role.setItems(Role.values());
        role.setItemLabelGenerator(r -> r == Role.ADMIN ? "Ylläpitäjä (ADMIN)" : "Käyttäjä (USER)");
        role.setAllowCustomValue(false);
        if (editingSelf) {
            // Oman roolin vaihtaminen vahingossa voisi lukita ylläpitäjän ulos
            role.setEnabled(false);
            role.setHelperText("Omaa rooliasi ei voi muuttaa");
        }

        binder.forField(username)
                .asRequired("Käyttäjätunnus on pakollinen")
                .withValidator(new StringLengthValidator("Enintään 50 merkkiä", 0, 50))
                .bind(f -> f.username, (f, v) -> f.username = v);
        binder.forField(email)
                .asRequired("Sähköposti on pakollinen")
                .withValidator(new EmailValidator("Anna kelvollinen sähköpostiosoite"))
                .bind(f -> f.email, (f, v) -> f.email = v);
        binder.forField(role)
                .asRequired("Valitse rooli")
                .bind(f -> f.role, (f, v) -> f.role = v);

        FormLayout layout = new FormLayout(username, email, role);
        layout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        if (existing == null) {
            PasswordField password = new PasswordField("Salasana");
            password.setHelperText("Vähintään " + AppUserService.MIN_PASSWORD_LENGTH + " merkkiä");
            binder.forField(password)
                    .asRequired("Salasana on pakollinen")
                    .withValidator(new StringLengthValidator(
                            "Salasanan tulee olla vähintään " + AppUserService.MIN_PASSWORD_LENGTH
                                    + " merkkiä pitkä", AppUserService.MIN_PASSWORD_LENGTH, null))
                    .bind(f -> f.password, (f, v) -> f.password = v);
            layout.add(password);
        } else {
            form.username = existing.getUsername();
            form.email = existing.getEmail();
            form.role = existing.getRole();
        }
        binder.readBean(form);
        add(layout);

        Button save = new Button("Tallenna", e -> save());
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getFooter().add(new Button("Peruuta", e -> close()), save);
    }

    private void save() {
        if (!binder.writeBeanIfValid(form)) {
            return;
        }
        try {
            if (existing == null) {
                users.create(form.username, form.email, form.password, form.role);
            } else {
                users.update(existing.getId(), form.username, form.email, form.role);
            }
        } catch (RuntimeException ex) {
            Ui.error("Tallennus epäonnistui: " + ex.getMessage());
            return;
        }
        Ui.success("Käyttäjä tallennettu");
        close();
        onSaved.run();
    }
}

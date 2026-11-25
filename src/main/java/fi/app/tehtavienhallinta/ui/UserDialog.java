package fi.app.tehtavienhallinta.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.data.validator.StringLengthValidator;
import fi.app.tehtavienhallinta.AppUser;
import fi.app.tehtavienhallinta.AppUserService;
import fi.app.tehtavienhallinta.Role;
import fi.app.tehtavienhallinta.UserProfile;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.data.validator.RegexpValidator;

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
        String fullName;
        String phone;
        String jobTitle;
        String bio;
        String website;
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
        setWidth("32em");

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
                .withValidator(new StringLengthValidator("Käyttäjätunnuksen tulee olla 3-50 merkkiä", 3, 50))
                .withValidator(new RegexpValidator("Vain kirjaimia, numeroita sekä merkkejä . _ -",
                        "^[a-zA-Z0-9._-]+$"))
                .bind(f -> f.username, (f, v) -> f.username = v);
        binder.forField(email)
                .asRequired("Sähköposti on pakollinen")
                .withValidator(new EmailValidator("Anna kelvollinen sähköpostiosoite"))
                .bind(f -> f.email, (f, v) -> f.email = v);
        binder.forField(role)
                .asRequired("Valitse rooli")
                .bind(f -> f.role, (f, v) -> f.role = v);

        TextField fullName = new TextField("Koko nimi");
        fullName.setMaxLength(100);
        TextField phone = new TextField("Puhelin");
        phone.setPlaceholder("+358 40 123 4567");
        phone.setMaxLength(20);
        TextField jobTitle = new TextField("Tehtävänimike");
        jobTitle.setMaxLength(100);
        TextField website = new TextField("Verkkosivu");
        website.setPlaceholder("https://");
        website.setMaxLength(200);
        TextArea bio = new TextArea("Esittely");
        bio.setMaxLength(500);

        binder.forField(fullName)
                .asRequired("Koko nimi on pakollinen")
                .withValidator(new StringLengthValidator("Nimen tulee olla 2-100 merkkiä", 2, 100))
                .bind(f -> f.fullName, (f, v) -> f.fullName = v);
        binder.forField(phone)
                .withValidator(v -> v == null || v.isBlank() || v.matches(UserProfile.PHONE_PATTERN),
                        "Anna kelvollinen puhelinnumero")
                .bind(f -> f.phone, (f, v) -> f.phone = v);
        binder.forField(jobTitle)
                .withValidator(new StringLengthValidator("Enintään 100 merkkiä", 0, 100))
                .bind(f -> f.jobTitle, (f, v) -> f.jobTitle = v);
        binder.forField(website)
                .withValidator(v -> v == null || v.isBlank() || v.matches(UserProfile.WEBSITE_PATTERN),
                        "Osoitteen tulee alkaa http:// tai https://")
                .bind(f -> f.website, (f, v) -> f.website = v);
        binder.forField(bio)
                .withValidator(new StringLengthValidator("Enintään 500 merkkiä", 0, 500))
                .bind(f -> f.bio, (f, v) -> f.bio = v);

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
        layout.add(new H4("Profiili"), fullName, phone, jobTitle, website, bio);
        UserProfile profile = existing == null ? null : existing.getProfile();
        if (profile != null) {
            form.fullName = profile.getFullName();
            form.phone = profile.getPhone();
            form.jobTitle = profile.getJobTitle();
            form.bio = profile.getBio();
            form.website = profile.getWebsite();
        }
        binder.readBean(form);
        add(layout);

        Button save = new Button("Tallenna", e -> save());
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getFooter().add(new Button("Peruuta", e -> close()), save);
    }

    private UserProfile profileFromForm() {
        UserProfile profile = new UserProfile();
        profile.setFullName(form.fullName);
        profile.setPhone(blankToNull(form.phone));
        profile.setJobTitle(blankToNull(form.jobTitle));
        profile.setBio(blankToNull(form.bio));
        profile.setWebsite(blankToNull(form.website));
        return profile;
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private void save() {
        if (!binder.writeBeanIfValid(form)) {
            return;
        }
        try {
            if (existing == null) {
                users.create(form.username, form.email, form.password, form.role, profileFromForm());
            } else {
                users.update(existing.getId(), form.username, form.email, form.role, profileFromForm());
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

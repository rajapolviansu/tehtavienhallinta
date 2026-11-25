package fi.app.tehtavienhallinta.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import fi.app.tehtavienhallinta.AppUserService;

/**
 * Rekisteröitymisnäkymä. Luo uuden tavallisen käyttäjän (rooli USER) ja ohjaa kirjautumiseen.
 */
@Route("register")
@PageTitle("Rekisteröidy | Tehtävienhallinta")
@AnonymousAllowed
public class RegisterView extends VerticalLayout {

    private final AppUserService users;

    private final TextField username = new TextField("Käyttäjätunnus");
    private final EmailField email = new EmailField("Sähköposti");
    private final PasswordField password = new PasswordField("Salasana");
    private final PasswordField confirm = new PasswordField("Salasana uudelleen");

    public RegisterView(AppUserService users) {
        this.users = users;

        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        username.setMinLength(3);
        username.setMaxLength(50);
        username.setPattern("^[a-zA-Z0-9._-]+$");
        username.setHelperText("3–50 merkkiä: kirjaimia, numeroita sekä . _ -");
        username.setRequired(true);
        email.setRequired(true);
        email.setErrorMessage("Anna kelvollinen sähköpostiosoite");
        password.setMinLength(AppUserService.MIN_PASSWORD_LENGTH);
        password.setHelperText("Vähintään " + AppUserService.MIN_PASSWORD_LENGTH + " merkkiä");
        password.setRequired(true);
        confirm.setRequired(true);

        Button submit = new Button("Rekisteröidy", e -> register());
        submit.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        VerticalLayout form = new VerticalLayout(username, email, password, confirm, submit,
                new RouterLink("Onko sinulla jo tunnus? Kirjaudu sisään", LoginView.class));
        form.setWidth("22em");
        form.setMaxWidth("100%");
        form.setPadding(false);

        add(new H1("Tehtävienhallinta"), form);
    }

    private void register() {
        String name = username.getValue().trim();
        String mail = email.getValue().trim();

        if (name.isEmpty() || mail.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            Notification.show("Täytä kaikki kentät");
            return;
        }
        if (username.isInvalid() || email.isInvalid() || password.isInvalid()) {
            Notification.show("Tarkista lomakkeen tiedot");
            return;
        }
        if (!password.getValue().equals(confirm.getValue())) {
            confirm.setInvalid(true);
            confirm.setErrorMessage("Salasanat eivät täsmää");
            return;
        }
        try {
            users.register(name, mail, password.getValue());
        } catch (IllegalArgumentException ex) {
            Notification.show(ex.getMessage());
            return;
        } catch (RuntimeException ex) {
            // esim. ConstraintViolationException entiteetin validoinnista
            Notification.show("Rekisteröityminen epäonnistui. Tarkista tiedot.");
            return;
        }
        Notification.show("Tunnus luotu. Voit nyt kirjautua sisään.");
        getUI().ifPresent(ui -> ui.navigate(LoginView.class));
    }
}

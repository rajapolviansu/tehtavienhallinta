package fi.app.tehtavienhallinta.ui;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * Kirjautumisnäkymä. Lomake lähettää tunnuksen ja salasanan Spring Securityn
 * /login-osoitteeseen, joten kirjautumislogiikkaa ei tarvitse kirjoittaa itse.
 */
@Route("login")
@PageTitle("Kirjaudu | Tehtävienhallinta")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm login = new LoginForm();

    public LoginView() {
        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        login.setAction("login");
        login.setI18n(finnishI18n());

        add(new H1("Tehtävienhallinta"), login,
                new RouterLink("Ei vielä tunnusta? Rekisteröidy", RegisterView.class));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Spring Security ohjaa epäonnistuneen kirjautumisen takaisin tänne ?error-parametrilla
        if (event.getLocation().getQueryParameters().getParameters().containsKey("error")) {
            login.setError(true);
        }
    }

    private static LoginI18n finnishI18n() {
        LoginI18n.Form form = new LoginI18n.Form();
        form.setTitle("Kirjaudu sisään");
        form.setUsername("Käyttäjätunnus");
        form.setPassword("Salasana");
        form.setSubmit("Kirjaudu");

        LoginI18n.ErrorMessage error = new LoginI18n.ErrorMessage();
        error.setTitle("Kirjautuminen epäonnistui");
        error.setMessage("Tarkista käyttäjätunnus ja salasana.");

        LoginI18n i18n = new LoginI18n();
        i18n.setForm(form);
        i18n.setErrorMessage(error);
        return i18n;
    }
}

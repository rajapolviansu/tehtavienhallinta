package fi.app.tehtavienhallinta.ui;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import fi.app.tehtavienhallinta.Role;
import jakarta.annotation.security.PermitAll;

/**
 * Oma tili. Kaikille kirjautuneille käyttäjille (USER, SUPER ja ADMIN).
 */
@Route(value = "tili", layout = MainLayout.class)
@PageTitle("Oma tili | Tehtävienhallinta")
@PermitAll
public class AccountView extends VerticalLayout {

    public AccountView(AuthenticationContext authContext) {
        String role = authContext.hasRole("ADMIN") ? Role.ADMIN.getLabel()
                : authContext.hasRole("SUPER") ? Role.SUPER.getLabel() : Role.USER.getLabel();
        add(new H2("Oma tili"),
                new Paragraph("Käyttäjätunnus: " + authContext.getPrincipalName().orElse("")),
                new Paragraph("Rooli: " + role));
    }
}

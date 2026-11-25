package fi.app.tehtavienhallinta.ui;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

/**
 * Tiimisivu. Vain SUPER- ja USER-rooleille; ylläpitäjällä on oma näkymänsä (UsersView).
 */
@Route(value = "tiimi", layout = MainLayout.class)
@PageTitle("Tiimi | Tehtävienhallinta")
@RolesAllowed({"SUPER", "USER"})
public class TeamView extends VerticalLayout {

    public TeamView() {
        add(new H2("Tiimi"), new Paragraph("Tämä sivu näkyy vain super- ja user-käyttäjille."));
    }
}

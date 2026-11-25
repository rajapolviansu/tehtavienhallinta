package fi.app.tehtavienhallinta.ui;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * Päänäkymä (etusivu). Avoin kaikille, myös kirjautumattomille vierailijoille.
 */
@Route(value = "", layout = MainLayout.class)
@PageTitle("Etusivu | Tehtävienhallinta")
@AnonymousAllowed
public class HomeView extends VerticalLayout {

    public HomeView() {
        add(new H2("Tervetuloa Tehtävienhallintaan"),
                new Paragraph("Hallitse projekteja, tehtäviä ja tunnisteita. "
                        + "Kirjaudu sisään nähdäksesi omat tietosi."));
    }
}

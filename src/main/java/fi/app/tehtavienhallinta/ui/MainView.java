package fi.app.tehtavienhallinta.ui;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Sovelluksen ainoa näkymä (SPA). Välilehdet ja panelit lisätään kohdissa 5–6.
 */
@Route("")
@PageTitle("Tehtävienhallinta")
public class MainView extends VerticalLayout {

    public MainView() {
        add(new H1("Tehtävienhallinta"));
    }
}

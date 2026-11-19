package fi.app.tehtavienhallinta.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.PermitAll;

/**
 * Sovelluksen ainoa näkymä (SPA). Ylhäällä näkyy kirjautunut käyttäjä ja uloskirjautumispainike,
 * alla välilehdet. Välilehtien sisällöt (paneelit) lisätään kohdissa 5–6.
 */
@Route("")
@PageTitle("Tehtävienhallinta")
@PermitAll
public class MainView extends VerticalLayout {

    public MainView(AuthenticationContext authContext, TasksPanel tasksPanel) {
        String username = authContext.getPrincipalName().orElse("");
        Button logout = new Button("Kirjaudu ulos", e -> authContext.logout());

        HorizontalLayout header = new HorizontalLayout(new H1("Tehtävienhallinta"),
                userInfo(username, logout));
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        TabSheet tabs = new TabSheet();
        tabs.setSizeFull();
        tabs.add("Tehtävät", tasksPanel);
        tabs.add("Projektit", placeholder("Projektit tulevat tähän."));
        tabs.add("Tunnisteet", placeholder("Tunnisteet tulevat tähän."));

        setSizeFull();
        add(header, tabs);
        setFlexGrow(1, tabs);
    }

    private static Div placeholder(String text) {
        Div panel = new Div(new Span(text));
        panel.getStyle().set("padding", "var(--lumo-space-m)");
        return panel;
    }

    private static HorizontalLayout userInfo(String username, Button logout) {
        HorizontalLayout box = new HorizontalLayout(new Span("Kirjautunut: " + username), logout);
        box.setAlignItems(FlexComponent.Alignment.CENTER);
        return box;
    }
}

package fi.app.tehtavienhallinta.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.ObjectProvider;

/**
 * Sovelluksen ainoa näkymä (SPA). Ylhäällä näkyy kirjautunut käyttäjä ja uloskirjautumispainike,
 * alla välilehdet: Tehtävät, Projektit, Tunnisteet ja (vain ylläpitäjälle) Käyttäjät.
 */
@Route("")
@PageTitle("Tehtävienhallinta")
@PermitAll
public class MainView extends VerticalLayout {

    public MainView(AuthenticationContext authContext,
                    TasksPanel tasksPanel,
                    SearchPanel searchPanel,
                    ProjectsPanel projectsPanel,
                    TagsPanel tagsPanel,
                    ObjectProvider<UsersPanel> usersPanel) {
        String username = authContext.getPrincipalName().orElse("");
        Button logout = new Button("Kirjaudu ulos", e -> authContext.logout());

        HorizontalLayout header = new HorizontalLayout(new H1("Tehtävienhallinta"),
                userInfo(username, logout));
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        TabSheet tabs = new TabSheet();
        tabs.setSizeFull();
        Tab tasksTab = tabs.add("Tehtävät", tasksPanel);
        Tab searchTab = tabs.add("Haku", searchPanel);
        tabs.add("Projektit", projectsPanel);
        tabs.add("Tunnisteet", tagsPanel);
        // Käyttäjäpaneeli luodaan vain ylläpitäjälle: palvelu hylkäisi muut
        if (authContext.hasRole("ADMIN")) {
            tabs.add("Käyttäjät", usersPanel.getObject());
        }

        // Muissa välilehdissä tehdyt muutokset (esim. projektin nimi) näkyvät tehtävälistassa heti
        tabs.addSelectedChangeListener(e -> {
            if (e.getSelectedTab() == tasksTab) {
                tasksPanel.refresh();
            } else if (e.getSelectedTab() == searchTab) {
                searchPanel.reloadOptions();
            }
        });

        setSizeFull();
        add(header, tabs);
        setFlexGrow(1, tabs);
    }

    private static HorizontalLayout userInfo(String username, Button logout) {
        HorizontalLayout box = new HorizontalLayout(new Span("Kirjautunut: " + username), logout);
        box.setAlignItems(FlexComponent.Alignment.CENTER);
        return box;
    }
}

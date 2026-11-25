package fi.app.tehtavienhallinta.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.tabs.TabSheetVariant;
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
        // 2c. Teemamuunnokset (addThemeVariants): pieni ja korostettu nappi
        logout.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
        // 5. Fade-in-animaatio + hover/focus (styles.css)
        logout.addClassName("fade-in-button");

        HorizontalLayout header = new HorizontalLayout(new H1("Tehtävienhallinta"),
                userInfo(username, logout));
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        // 2b. Inline-tyyli getStyle().set()
        header.getStyle().set("border-bottom", "3px solid var(--app-gold)");
        header.getStyle().set("padding-bottom", "var(--lumo-space-s)");

        TabSheet tabs = new TabSheet();
        tabs.setSizeFull();
        tabs.addThemeVariants(TabSheetVariant.LUMO_TABS_CENTERED);
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
        // 2a + 5. CSS-luokka: kortti zoomaa 3 % hoverissa
        box.addClassName("zoom-card");
        box.getStyle().set("padding", "var(--lumo-space-s) var(--lumo-space-m)");
        box.getStyle().set("background", "var(--app-green-100)");
        box.getStyle().set("border-radius", "var(--lumo-border-radius-l)");
        return box;
    }
}

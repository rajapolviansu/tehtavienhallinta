package fi.app.tehtavienhallinta.ui;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.time.Year;

/**
 * Sovelluksen yhteinen asettelu (SPA): header (otsikko ja kirjautunut käyttäjä), navigaatio
 * (sivuvalikko) ja footer. Kaikki näkymät käyttävät tätä asettelua
 * {@code @Route(value, layout = MainLayout.class)}-määrityksellä, ja vain sisältöalue vaihtuu
 * navigoitaessa.
 */
public class MainLayout extends AppLayout {

    public MainLayout(AuthenticationContext authContext) {
        setPrimarySection(Section.DRAWER);
        addToNavbar(createHeader(authContext));
        addToDrawer(createNavigation(authContext), createFooter());
    }

    private Header createHeader(AuthenticationContext authContext) {
        String username = authContext.getPrincipalName().orElse("");
        Button logout = new Button("Kirjaudu ulos", e -> authContext.logout());
        logout.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
        logout.addClassName("fade-in-button");

        HorizontalLayout userInfo = new HorizontalLayout(new Span("Kirjautunut: " + username), logout);
        userInfo.setAlignItems(FlexComponent.Alignment.CENTER);
        userInfo.addClassName("zoom-card");
        userInfo.getStyle().set("padding", "var(--lumo-space-s) var(--lumo-space-m)");
        userInfo.getStyle().set("background", "var(--app-green-100)");
        userInfo.getStyle().set("border-radius", "var(--lumo-border-radius-l)");

        H1 title = new H1("Tehtävienhallinta");
        title.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        HorizontalLayout row = new HorizontalLayout(new DrawerToggle(), title, userInfo);
        row.setWidthFull();
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.expand(title);
        row.addClassName(LumoUtility.Padding.Horizontal.MEDIUM);

        Header header = new Header(row);
        header.setWidthFull();
        header.getStyle().set("border-bottom", "3px solid var(--app-gold)");
        return header;
    }

    private SideNav createNavigation(AuthenticationContext authContext) {
        SideNav nav = new SideNav();
        nav.addItem(new SideNavItem("Tehtävät", TasksView.class, VaadinIcon.TASKS.create()));
        nav.addItem(new SideNavItem("Haku", SearchView.class, VaadinIcon.SEARCH.create()));
        nav.addItem(new SideNavItem("Projektit", ProjectsView.class, VaadinIcon.FOLDER.create()));
        nav.addItem(new SideNavItem("Tunnisteet", TagsView.class, VaadinIcon.TAGS.create()));
        // Linkki näytetään vain ylläpitäjälle (näkymä on lisäksi suojattu @RolesAllowed:lla)
        if (authContext.hasRole("ADMIN")) {
            nav.addItem(new SideNavItem("Käyttäjät", UsersView.class, VaadinIcon.USERS.create()));
        }
        return nav;
    }

    private Footer createFooter() {
        Footer footer = new Footer(new Span("© " + Year.now() + " Tehtävienhallinta"));
        footer.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.SMALL,
                LumoUtility.Padding.MEDIUM);
        // Työntää footerin sivuvalikon alareunaan
        footer.getStyle().set("margin-top", "auto");
        return footer;
    }
}

package fi.app.tehtavienhallinta.ui;

import fi.app.tehtavienhallinta.Role;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;

import java.time.Year;

/**
 * Sovelluksen yhteinen asettelu (SPA): header (otsikko ja kirjautunut käyttäjä), navigaatio
 * (sivuvalikko) ja footer. Kaikki näkymät käyttävät tätä asettelua
 * {@code @Route(value, layout = MainLayout.class)}-määrityksellä, ja vain sisältöalue vaihtuu
 * navigoitaessa.
 */
@PermitAll
public class MainLayout extends AppLayout {

    public MainLayout(AuthenticationContext authContext) {
        setPrimarySection(Section.DRAWER);
        addToNavbar(createHeader(authContext));
        addToDrawer(createNavigation(authContext));
        // Footer lisätään layoutin juureen; tyylit kiinnittävät sen näkymän alareunaan
        getElement().appendChild(createFooter().getElement());
    }

    private Header createHeader(AuthenticationContext authContext) {
        String username = authContext.getPrincipalName().orElse("");
        boolean loggedIn = authContext.isAuthenticated();
        String role = authContext.hasRole("ADMIN") ? Role.ADMIN.getLabel()
                : authContext.hasRole("SUPER") ? Role.SUPER.getLabel() : Role.USER.getLabel();

        // Teksti omassa span-elementissä, jotta se voidaan piilottaa kapealla näytöllä (vain ikoni jää)
        Span logoutLabel = new Span("Kirjaudu ulos");
        logoutLabel.addClassName("header-logout-label");
        Button logout = new Button(VaadinIcon.SIGN_OUT.create(), e -> authContext.logout());
        logout.getElement().appendChild(logoutLabel.getElement());
        logout.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
        logout.addClassName("fade-in-button");
        logout.setAriaLabel("Kirjaudu ulos");

        Avatar avatar = new Avatar(username);
        avatar.setTooltipEnabled(false);
        Span name = new Span(username);
        name.addClassNames(LumoUtility.FontWeight.SEMIBOLD, LumoUtility.FontSize.SMALL);
        Span roleLabel = new Span(role);
        roleLabel.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL);
        Div userText = new Div(name, roleLabel);
        userText.addClassName("header-user-text");
        userText.getStyle().set("display", "flex");
        userText.getStyle().set("flex-direction", "column");
        userText.getStyle().set("line-height", "1.2");

        HorizontalLayout userInfo = new HorizontalLayout(avatar, userText, logout);
        userInfo.setAlignItems(FlexComponent.Alignment.CENTER);
        userInfo.addClassNames("zoom-card", "header-user-info");
        userInfo.getStyle().set("padding", "var(--lumo-space-xs) var(--lumo-space-m)");
        userInfo.getStyle().set("background", "var(--app-green-100)");
        userInfo.getStyle().set("border-radius", "var(--lumo-border-radius-l)");

        Icon logo = VaadinIcon.CHECK_CIRCLE.create();
        logo.setColor("var(--app-gold)");
        H1 title = new H1("Tehtävienhallinta");
        title.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);
        HorizontalLayout brand = new HorizontalLayout(logo, title);
        brand.setAlignItems(FlexComponent.Alignment.CENTER);
        brand.setSpacing(false);
        brand.addClassNames(LumoUtility.Gap.SMALL, "header-brand");

        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Avaa tai sulje valikko");

        // Kirjautumaton vierailija näkee kirjautumislinkin käyttäjätietojen sijaan
        Component right = loggedIn ? userInfo : new RouterLink("Kirjaudu sisään", LoginView.class);
        HorizontalLayout row = new HorizontalLayout(toggle, brand, right);
        row.setWidthFull();
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.expand(brand);
        row.addClassName(LumoUtility.Padding.Horizontal.MEDIUM);

        Header header = new Header(row);
        header.setWidthFull();
        header.getStyle().set("border-bottom", "3px solid var(--app-gold)");
        return header;
    }

    private SideNav createNavigation(AuthenticationContext authContext) {
        SideNav nav = new SideNav();
        // Etusivu on kaikille avoin
        nav.addItem(new SideNavItem("Etusivu", HomeView.class, VaadinIcon.HOME.create()));
        if (!authContext.isAuthenticated()) {
            return nav;
        }
        nav.addItem(new SideNavItem("Oma tili", AccountView.class, VaadinIcon.USER.create()));
        nav.addItem(new SideNavItem("Tehtävät", TasksView.class, VaadinIcon.TASKS.create()));
        nav.addItem(new SideNavItem("Kojelauta", DashboardView.class, VaadinIcon.DASHBOARD.create()));
        nav.addItem(new SideNavItem("Taulu", BoardView.class, VaadinIcon.GRID_BIG.create()));
        nav.addItem(new SideNavItem("Projektinäkymä", ProjectDetailView.class, VaadinIcon.SPLIT.create()));
        nav.addItem(new SideNavItem("Haku", SearchView.class, VaadinIcon.SEARCH.create()));
        nav.addItem(new SideNavItem("Projektit", ProjectsView.class, VaadinIcon.FOLDER.create()));
        nav.addItem(new SideNavItem("Tunnisteet", TagsView.class, VaadinIcon.TAGS.create()));
        // Linkit näytetään vain oikeutetuille (näkymät on lisäksi suojattu @RolesAllowed:lla)
        if (authContext.hasRole("SUPER") || authContext.hasRole("USER")) {
            nav.addItem(new SideNavItem("Tiimi", TeamView.class, VaadinIcon.GROUP.create()));
        }
        if (authContext.hasRole("ADMIN")) {
            nav.addItem(new SideNavItem("Käyttäjät", UsersView.class, VaadinIcon.USERS.create()));
        }
        return nav;
    }

    /**
     * Footer on kiinnitetty näkymän alareunaan (CSS: position fixed) ja erottuu tummalla
     * taustalla. Kapealla näytöllä sisältö rivittyy keskitetysti.
     */
    private Footer createFooter() {
        Span copyright = new Span("© " + Year.now() + " Ansu Rajapolvi · Tehtävienhallinta");
        copyright.addClassName("app-footer-text");

        Anchor source = footerLink("Lähdekoodi (GitHub)", "https://github.com/rajapolviansu/tehtavienhallinta",
                VaadinIcon.CODE.create());
        Anchor vaadin = footerLink("Vaadin", "https://vaadin.com", VaadinIcon.EXTERNAL_LINK.create());
        RouterLink search = new RouterLink("Haku", SearchView.class);
        search.addClassName("app-footer-link");

        Div links = new Div(source, vaadin, search);
        links.addClassName("app-footer-links");

        Footer footer = new Footer(copyright, links);
        footer.addClassName("app-footer");
        return footer;
    }

    private Anchor footerLink(String text, String href, Icon icon) {
        icon.setSize("var(--lumo-font-size-s)");
        Anchor a = new Anchor(href, icon, new Span(text));
        a.setTarget("_blank");
        a.getElement().setAttribute("rel", "noopener noreferrer");
        a.addClassName("app-footer-link");
        return a;
    }
}

package fi.app.tehtavienhallinta.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import com.vaadin.flow.spring.security.AuthenticationContext;
import fi.app.tehtavienhallinta.AppUser;
import fi.app.tehtavienhallinta.AppUserService;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;

/**
 * Käyttäjien hallinta. Vain ADMIN: palvelu hylkää muut, ja pääsy näkymään on rajattu ylläpitäjälle
 * (@RolesAllowed) eikä valikko näytä linkkiä muille.
 */
@Route(value = "kayttajat", layout = MainLayout.class)
@PageTitle("Käyttäjät | Tehtävienhallinta")
@RolesAllowed("ADMIN")
public class UsersView extends VerticalLayout {

    private static final DateTimeFormatter DATE =
            DateTimeFormatter.ofPattern("d.M.yyyy").withZone(ZoneId.systemDefault());

    private final AppUserService users;
    private final String currentUsername;
    private final Grid<AppUser> grid = new Grid<>(AppUser.class, false);

    public UsersView(AppUserService users, AuthenticationContext authContext) {
        this.users = users;
        this.currentUsername = authContext.getPrincipalName().orElse("");

        setSizeFull();
        setPadding(false);

        Button add = new Button("Uusi käyttäjä", VaadinIcon.PLUS.create(), e -> openDialog(null));
        add.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        configureGrid();
        add(add, grid);
        setFlexGrow(1, grid);
        refresh();
    }

    private void configureGrid() {
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.setSizeFull();

        grid.addColumn(AppUser::getUsername).setHeader("Käyttäjätunnus").setFlexGrow(1)
                .setComparator(Comparator.comparing(AppUser::getUsername, String.CASE_INSENSITIVE_ORDER));
        grid.addColumn(AppUser::getEmail).setHeader("Sähköposti").setFlexGrow(2);
        grid.addColumn(u -> u.getProfile() == null ? "" : u.getProfile().getFullName())
                .setHeader("Nimi (profiili)").setFlexGrow(1);
        grid.addColumn(u -> u.getProfile() == null || u.getProfile().getJobTitle() == null
                        ? "" : u.getProfile().getJobTitle())
                .setHeader("Tehtävänimike").setFlexGrow(1);
        grid.addColumn(u -> u.getRole().name()).setHeader("Rooli").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(u -> DATE.format(u.getCreated())).setHeader("Luotu").setAutoWidth(true)
                .setFlexGrow(0).setComparator(Comparator.comparing(AppUser::getCreated));
        grid.addColumn(new ComponentRenderer<>(user -> {
            // Omaa tiliä ei voi poistaa tästä, jottei ylläpitäjä poista itseään vahingossa
            boolean self = user.getUsername().equals(currentUsername);
            return Ui.rowActions(() -> openDialog(user), () -> confirmDelete(user), !self);
        })).setHeader("").setAutoWidth(true).setFlexGrow(0);
    }

    void refresh() {
        grid.setItems(users.findAll());
    }

    private void openDialog(AppUser user) {
        boolean editingSelf = user != null && user.getUsername().equals(currentUsername);
        new UserDialog(users, user, editingSelf, this::refresh).open();
    }

    private void confirmDelete(AppUser user) {
        Ui.confirmDelete("Poistetaanko käyttäjä?",
                "Käyttäjä \"" + user.getUsername() + "\" poistetaan pysyvästi.",
                () -> Ui.runDelete(() -> users.delete(user.getId()), "Käyttäjä poistettu", this::refresh));
    }
}

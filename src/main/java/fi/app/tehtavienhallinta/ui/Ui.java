package fi.app.tehtavienhallinta.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

/**
 * Käyttöliittymän yhteiset apumetodit: ilmoitukset, poistovahvistus ja rivin toimintopainikkeet.
 */
final class Ui {

    private Ui() {
    }

    static void success(String message) {
        Notification.show(message).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    static void error(String message) {
        Notification.show(message, 5000, Notification.Position.BOTTOM_START)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    /** Kysyy vahvistuksen ja suorittaa poiston vain, jos käyttäjä vahvistaa. */
    static void confirmDelete(String header, String text, Runnable onConfirm) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader(header);
        dialog.setText(text);
        dialog.setCancelable(true);
        dialog.setCancelText("Peruuta");
        dialog.setConfirmText("Poista");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(e -> onConfirm.run());
        dialog.open();
    }

    /** Muokkaa- ja Poista-painikkeet taulukon riville. */
    static HorizontalLayout rowActions(Runnable onEdit, Runnable onDelete, boolean deleteEnabled) {
        Button edit = new Button(VaadinIcon.EDIT.create(), e -> onEdit.run());
        edit.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        edit.setAriaLabel("Muokkaa");
        Button delete = new Button(VaadinIcon.TRASH.create(), e -> onDelete.run());
        delete.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        delete.setAriaLabel("Poista");
        delete.setEnabled(deleteEnabled);
        return new HorizontalLayout(edit, delete);
    }

    /** Poistaa ja ilmoittaa lopputuloksen; virheestä näytetään palvelun viesti. */
    static void runDelete(Runnable delete, String successMessage, Runnable afterwards) {
        try {
            delete.run();
            success(successMessage);
        } catch (RuntimeException ex) {
            error("Poisto epäonnistui: " + ex.getMessage());
        }
        afterwards.run();
    }
}

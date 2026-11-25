package fi.app.tehtavienhallinta.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import fi.app.tehtavienhallinta.Tag;
import fi.app.tehtavienhallinta.TagService;

import java.util.Comparator;

/**
 * Tunnisteiden lista: lisäys, muokkaus ja poisto (poisto irrottaa tunnisteen kaikista tehtävistä).
 */
@SpringComponent
@UIScope
public class TagsPanel extends VerticalLayout {

    private final TagService tags;
    private final Grid<Tag> grid = new Grid<>(Tag.class, false);

    public TagsPanel(TagService tags) {
        this.tags = tags;

        setSizeFull();
        setPadding(false);

        Button add = new Button("Uusi tunniste", VaadinIcon.PLUS.create(), e -> openDialog(null));
        add.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        configureGrid();
        add(add, grid);
        setFlexGrow(1, grid);
        refresh();
    }

    private void configureGrid() {
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.setSizeFull();

        grid.addColumn(Tag::getName).setHeader("Nimi").setFlexGrow(2)
                .setComparator(Comparator.comparing(Tag::getName, String.CASE_INSENSITIVE_ORDER));
        grid.addColumn(new ComponentRenderer<>(TagsPanel::colorCell)).setHeader("Väri").setFlexGrow(1);
        grid.addColumn(t -> t.getCategory() == null ? "" : t.getCategory()).setHeader("Luokka").setFlexGrow(1);
        grid.addColumn(t -> t.getDescription() == null ? "" : t.getDescription())
                .setHeader("Kuvaus").setFlexGrow(2);
        grid.addColumn(Tag::getSortOrder).setHeader("Järjestys").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(new ComponentRenderer<>(tag ->
                Ui.rowActions(() -> openDialog(tag), () -> confirmDelete(tag), true)))
                .setHeader("").setAutoWidth(true).setFlexGrow(0);
    }

    /** Värinäyte ja koodi. Väriä käytetään tyylissä vain, jos se on kelvollinen #rrggbb-arvo. */
    private static HorizontalLayout colorCell(Tag tag) {
        HorizontalLayout cell = new HorizontalLayout();
        cell.setAlignItems(FlexComponent.Alignment.CENTER);
        String color = tag.getColor();
        if (color != null && color.matches(TagDialog.COLOR_PATTERN)) {
            Span swatch = new Span();
            swatch.getStyle().set("display", "inline-block")
                    .set("width", "1.2em").set("height", "1.2em")
                    .set("border-radius", "50%").set("background", color);
            cell.add(swatch);
        }
        cell.add(new Span(color == null ? "" : color));
        return cell;
    }

    void refresh() {
        grid.setItems(tags.findAll());
    }

    private void openDialog(Tag tag) {
        new TagDialog(tags, tag, this::refresh).open();
    }

    private void confirmDelete(Tag tag) {
        Ui.confirmDelete("Poistetaanko tunniste?",
                "Tunniste \"" + tag.getName() + "\" poistetaan ja irrotetaan kaikista tehtävistä.",
                () -> Ui.runDelete(() -> tags.delete(tag.getId()), "Tunniste poistettu", this::refresh));
    }
}

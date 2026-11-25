package fi.app.tehtavienhallinta.ui;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.AccessDeniedException;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.HasErrorParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.RouterLink;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Näytetään, kun kirjautunut käyttäjä yrittää sivulle, johon hänen roolillaan ei ole oikeutta
 * (esim. USER ylläpitäjän käyttäjänhallintaan). Kirjautumattomat ohjataan kirjautumiseen.
 */
@ParentLayout(MainLayout.class)
@PageTitle("Ei käyttöoikeutta | Tehtävienhallinta")
public class AccessDeniedView extends VerticalLayout implements HasErrorParameter<AccessDeniedException> {

    @Override
    public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<AccessDeniedException> parameter) {
        removeAll();
        add(new H2("Ei käyttöoikeutta"),
                new Paragraph("Sinulla ei ole tarvittavia oikeuksia tälle sivulle. "
                        + "Jos uskot tämän olevan virhe, ota yhteyttä ylläpitäjään."),
                new RouterLink("Palaa etusivulle", HomeView.class));
        return HttpServletResponse.SC_FORBIDDEN;
    }
}

package nl.rotterdam.verbonden.core.features.marriage_intake.ui;

import nl.rotterdam.nl_design_system.wicket.components.button.RdButton;
import nl.rotterdam.nl_design_system.wicket.components.heading.RdHeading;
import nl.rotterdam.verbonden.core.burger_common.BurgerBasePage;
import nl.rotterdam.verbonden.core.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierAccessOutcome;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.util.UUID;

import static nl.rotterdam.verbonden.core.features.marriage_intake.ui.DossierPageParameterUtil.extractDossierId;
import static nl.rotterdam.verbonden.core.features.marriage_intake.ui.DossierPageParameterUtil.makeDossierPageParameters;
import static nl.rotterdam.verbonden.core.features.marriage_intake.ui.MarriageIntakeHeaderItems.MARRIAGE_INTAKE_CSS;

/**
 * Bevestigingsstap voor een burger die via de dossierlink van zijn partner binnenkomt en nog geen
 * eigen dossier heeft. Pas na het versturen van het formulier (POST) wordt de burger als tweede
 * partner gekoppeld; alleen de link openen (GET) verandert niets. Toont bewust geen gegevens uit
 * het dossier: die zijn pas zichtbaar na bevestigen.
 */
public class DossierUitnodigingPage extends BurgerBasePage {

    @SpringBean
    private MarriageIntakeService marriageIntakeService;

    private UUID dossierId;

    @Override
    protected IModel<String> getTitleModel() {
        return new ResourceModel("intake.page.title.uitnodiging");
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        UUID requested = extractDossierId(getPageParameters());
        if (requested == null
                || marriageIntakeService.resolveAccess(requested, getCurrentBsn()).scenario()
                        != DossierAccessOutcome.Scenario.INVITED) {
            // Al gekoppeld, eigen dossier of geen toegang: dat handelt de gewone intakepagina af.
            throw new RestartResponseException(MarriageIntakePage.class, makeDossierPageParameters(requested));
        }
        dossierId = requested;

        pageBody.add(new RdHeading("heading", getString("intake.uitnodiging.heading"), 1));

        Form<Void> form = new Form<>("form");
        pageBody.add(form);
        form.add(new RdButton("accepteerButton", new ResourceModel("intake.uitnodiging.accepteer")) {
            @Override
            public void onSubmit() {
                marriageIntakeService.acceptInvitation(dossierId, getCurrentBsn());
                setResponsePage(MarriageIntakePage.class, makeDossierPageParameters(dossierId));
            }
        });

        // Zonder dossierparameter: dan kan de burger een eigen dossier aanmaken.
        pageBody.add(new BookmarkablePageLink<>("weigerLink", MarriageIntakePage.class));
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(MARRIAGE_INTAKE_CSS);
    }
}

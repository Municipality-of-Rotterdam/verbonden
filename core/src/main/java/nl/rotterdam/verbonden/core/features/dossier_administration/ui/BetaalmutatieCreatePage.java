package nl.rotterdam.verbonden.core.features.dossier_administration.ui;

import nl.rotterdam.nl_design_system.wicket.components.button.RdButton;
import nl.rotterdam.nl_design_system.wicket.components.form_field_text_input.RdFormFieldTextInput;
import nl.rotterdam.verbonden.core.domain.Bedrag;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.BetaalmutatieNietMogelijkException;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.CreateBetaalmutatieDto;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.LambdaModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * Een bedrag toevoegen dat het paar na het indienen nog online moet betalen, bijvoorbeeld voor een bijbesteld
 * trouwboekje of alsnog een internationale akte.
 */
public class BetaalmutatieCreatePage extends DossierBeheerBasePage {

    public BetaalmutatieCreatePage(PageParameters params) {
        super(params);
        pageBody.add(
                new Label("titel", "Betaalmutatie toevoegen - dossier " + dossierId),
                new BookmarkablePageLink<>("terugLink", DossierDetailPage.class, parametersVoor(dossierId)),
                new FeedbackPanel("feedback"),
                new CreateBetaalmutatieForm("betaalmutatieForm")
        );
    }

    private class CreateBetaalmutatieForm extends Form<BetaalmutatieFormDto> {

        CreateBetaalmutatieForm(String id) {
            super(id, Model.of(new BetaalmutatieFormDto()));
        }

        @Override
        protected void onInitialize() {
            super.onInitialize();
            add(
                    new RdFormFieldTextInput<>("omschrijving",
                            LambdaModel.of(getModel(), BetaalmutatieFormDto::getOmschrijving,
                                    BetaalmutatieFormDto::setOmschrijving),
                            Model.of("Omschrijving"),
                            Model.of("Wat het paar ziet bij \"Te betalen\", bijv. \"Trouwboekje Luxe (bijbesteld)\""))
                            .setRequired(true),
                    new RdFormFieldTextInput<>("bedrag",
                            LambdaModel.of(getModel(), BetaalmutatieFormDto::getBedrag, BetaalmutatieFormDto::setBedrag),
                            Model.of("Bedrag"),
                            Model.of("Bedrag in euro's, bijv. 25.00"))
                            .setModelType(Bedrag.class)
                            .setRequired(true),
                    new RdButton("toevoegen", Model.of("Toevoegen"))
            );
        }

        @Override
        protected void onSubmit() {
            BetaalmutatieFormDto form = getModelObject();
            try {
                dossierAdministrationService.createBetaalmutatie(dossierId,
                        new CreateBetaalmutatieDto(form.getOmschrijving(), form.getBedrag()));
                naarDetailPagina();
            } catch (BetaalmutatieNietMogelijkException e) {
                error(e.getMessage());
            }
        }
    }
}

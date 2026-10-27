package nl.rotterdam.verbonden.core.features.marriage_intake.ui;

import nl.rotterdam.verbonden.core.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierNietCompleetException;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierNietWijzigbaarException;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierSamenvattingDto;
import nl.rotterdam.nl_design_system.wicket.components.button.RdButton;
import nl.rotterdam.nl_design_system.wicket.components.heading.RdHeading;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

public class BetalingPage extends IntakeBasePage {

    @SpringBean
    private MarriageIntakeService marriageIntakeService;

    private IModel<DossierSamenvattingDto> dossierModel;

    @Override
    protected IntakeStep getActiveStep() {
        return IntakeStep.CONTROLEREN;
    }

    @Override
    protected boolean hasStepNavigation() {
        return false;
    }

    @Override
    protected IModel<String> getTitleModel() {
        return new ResourceModel("betaling.page.title");
    }

    @Override
    protected IModel<DossierSamenvattingDto> getSidebarDossierModel() {
        return dossierModel();
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        pageBody.add(
                new RdHeading("heading", getString("betaling.heading"), 1),
                new Label("intro", new ResourceModel("betaling.intro")),
                new Link<Void>("betalingMislukt") {
                    @Override
                    public void onClick() {
                        setResponsePage(ControlerenPage.class, pageParametersForDossier(dossierId)
                                .add("betalingMislukt", true));
                    }
                }.add(new Label("label", new ResourceModel("betaling.mislukt.button"))),
                new BetalingForm("betalingForm")
        );
    }

    private IModel<DossierSamenvattingDto> dossierModel() {
        if (dossierModel == null) {
            dossierModel = LoadableDetachableModel.of(() -> marriageIntakeService.findByDossierId(dossierId));
        }
        return dossierModel;
    }

    private class BetalingForm extends Form<Void> {

        BetalingForm(String id) {
            super(id);
        }

        @Override
        protected void onInitialize() {
            super.onInitialize();
            add(new RdButton("betalingButton", new ResourceModel("betaling.uitvoeren")) {
                @Override
                public void onSubmit() {
                    try {
                        marriageIntakeService.dienIn(dossierModel.getObject().id());
                    } catch (DossierNietWijzigbaarException ignored) {
                        // The other partner may already have submitted this dossier.
                    } catch (DossierNietCompleetException ignored) {
                        setResponsePage(ControlerenPage.class, pageParametersForDossier(dossierId));
                        return;
                    }
                    AanvraagStatusPage.respond(dossierId);
                }

            });
        }
    }
}

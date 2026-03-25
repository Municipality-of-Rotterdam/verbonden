package nl.rotterdam.huwelijk.features.marriage_intake.ui;

import nl.rotterdam.huwelijk.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.DossierSamenvattingDto;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.RegistratieType;
import nl.rotterdam.nl_design_system.wicket.components.button.RdButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LambdaModel;
import org.apache.wicket.model.ResourceModel;

import java.util.function.Function;

public class IntakeSidebarPanel extends Panel {

    public IntakeSidebarPanel(String id, IModel<DossierSamenvattingDto> dossierModel) {
        super(id, dossierModel);
        setOutputMarkupId(true);
    }

    @SuppressWarnings("unchecked")
    private IModel<DossierSamenvattingDto> getDossierModel() {
        return (IModel<DossierSamenvattingDto>) getDefaultModel();
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        // Dé dag — registratie type row (visible when dossier is present)
        WebMarkupContainer dossierGegevens = new WebMarkupContainer("dossierGegevens") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(getDossierModel().getObject() != null);
            }
        };

        IModel<String> registratieModel = LambdaModel.of(
                () -> dossierValue(DossierSamenvattingDto::registratieType, RegistratieType::getLabel));
        dossierGegevens.add(new Label("registratieTypeLabel", registratieModel));

        // Ceremony row — only shown when a persisted dossier exists (id > 0)
        WebMarkupContainer ceremonieDossierGegevens = new WebMarkupContainer("ceremonieDossierGegevens") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                DossierSamenvattingDto d = getDossierModel().getObject();
                setVisible(d != null && d.id() > 0);
            }
        };
        IModel<String> ceremonieSoortModel = LambdaModel.of(
                () -> dossierValue(DossierSamenvattingDto::ceremonieSoort, CeremonieSoort::getLabel));
        IModel<String> ceremoniePrijsModel = LambdaModel.of(
                () -> dossierValue(DossierSamenvattingDto::ceremonieSoort, CeremonieSoort::getPrijs));
        ceremonieDossierGegevens.add(new Label("ceremonieSoortLabel", ceremonieSoortModel));
        ceremonieDossierGegevens.add(new Label("ceremoniePrijs", ceremoniePrijsModel));
        dossierGegevens.add(ceremonieDossierGegevens);

        add(dossierGegevens);

        // Bevestig form with RdButton
        Form<Void> bevestigForm = new Form<>("bevestigForm");
        bevestigForm.add(new RdButton("bevestigButton",
                new ResourceModel("intake.sidebar.bevestig")) {
            @Override
            public void onSubmit() {
                // TODO: navigation to confirmation — to be implemented in a future iteration
            }
        });
        add(bevestigForm);
    }

    private <I, O> String dossierValue(Function<DossierSamenvattingDto, I> extractor, Function<I, O> mapper) {
        DossierSamenvattingDto d = getDossierModel().getObject();
        if (d == null) {
            return "";
        }
        return String.valueOf(mapper.apply(extractor.apply(d)));
    }
}

package nl.rotterdam.verbonden.core.features.dossier_administration.ui;

import nl.rotterdam.nl_design_system.wicket.components.button.RdButton;
import nl.rotterdam.verbonden.core.administration_common.RdFormFieldSelect;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.ChangeCeremonieDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.RegistratieType;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.LambdaChoiceRenderer;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LambdaModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.List;

public class DossierCeremonieUpdatePage extends DossierBeheerBasePage {

    public DossierCeremonieUpdatePage(PageParameters params) {
        super(params);
        pageBody.add(
                new Label("titel", "Ceremonie wijzigen - dossier " + dossierId),
                new BookmarkablePageLink<>("terugLink", DossierDetailPage.class, parametersVoor(dossierId)),
                new FeedbackPanel("feedback"),
                new ChangeCeremonieForm("ceremonieForm")
        );
    }

    private class ChangeCeremonieForm extends Form<CeremonieFormDto> {

        ChangeCeremonieForm(String id) {
            super(id, Model.of(CeremonieFormDto.vanDto(dossier)));
        }

        @Override
        protected void onInitialize() {
            super.onInitialize();
            IModel<CeremonieFormDto> model = getModel();
            add(
                    new RdFormFieldSelect<>("registratieType",
                            LambdaModel.of(model, CeremonieFormDto::getRegistratieType, CeremonieFormDto::setRegistratieType),
                            Model.of("Soort registratie"),
                            List.of(RegistratieType.values()),
                            new LambdaChoiceRenderer<>(RegistratieType::getLabel, RegistratieType::name))
                            .setRequired(true),
                    new RdFormFieldSelect<>("ceremonieSoort",
                            LambdaModel.of(model, CeremonieFormDto::getCeremonieSoort, CeremonieFormDto::setCeremonieSoort),
                            Model.of("Ceremonie"),
                            List.of(CeremonieSoort.values()),
                            new LambdaChoiceRenderer<>(CeremonieSoort::getLabel, CeremonieSoort::name))
                            .setRequired(true),
                    new RdButton("opslaan", Model.of("Opslaan"))
            );
        }

        @Override
        protected void onSubmit() {
            CeremonieFormDto f = getModelObject();
            dossierAdministrationService.updateCeremonie(dossierId,
                    new ChangeCeremonieDto(f.getRegistratieType(), f.getCeremonieSoort()));
            naarDetailPagina();
        }
    }
}

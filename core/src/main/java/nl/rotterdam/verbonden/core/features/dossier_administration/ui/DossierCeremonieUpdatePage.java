package nl.rotterdam.verbonden.core.features.dossier_administration.ui;

import nl.rotterdam.nl_design_system.wicket.components.button.RdButton;
import nl.rotterdam.nl_design_system.wicket.components.form_field_text_input.RdFormFieldTextInput;
import nl.rotterdam.verbonden.core.administration_common.RdFormFieldSelect;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.ChangeCeremonieDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CeremonieTaal;
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
import java.util.Map;

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
                    new RdFormFieldSelect<>("taal",
                            LambdaModel.of(model, CeremonieFormDto::getTaal, CeremonieFormDto::setTaal),
                            Model.of("Taal van de ceremonie"),
                            List.of(CeremonieTaal.values()),
                            new LambdaChoiceRenderer<>(CeremonieTaal::getLabel, CeremonieTaal::name))
                            .setRequired(true),
                    new RdFormFieldTextInput<>("aantalBezoekers",
                            LambdaModel.of(model, CeremonieFormDto::getAantalBezoekers, CeremonieFormDto::setAantalBezoekers),
                            Model.of("Aantal bezoekers"),
                            Model.of("Maximaal 4 bij een klein en 16 bij een middelgroot huwelijk en bij een groot huwelijk is het maximum 100"))
                            .setHtmlInputType("number")
                            .setModelType(Integer.class)
                            .setRequired(true),
                    new RdButton("opslaan", Model.of("Opslaan"))
            );
        }

        @Override
        protected void onSubmit() {
            CeremonieFormDto f = getModelObject();
            if (f.getAantalBezoekers() < 0 || f.getAantalBezoekers() > f.getCeremonieSoort().getMaxAantalBezoekers()) {
                error( "Bij een " + f.getCeremonieSoort().getLabel().toLowerCase() + " huwelijk mogen maximaal "
                        + f.getCeremonieSoort().getMaxAantalBezoekers() + " bezoekers komen.");
                return;
            }
            dossierAdministrationService.updateCeremonie(dossierId,
                    new ChangeCeremonieDto(f.getRegistratieType(), f.getCeremonieSoort(), f.getTaal(),
                            f.getAantalBezoekers()));
            naarDetailPagina();
        }
    }
}

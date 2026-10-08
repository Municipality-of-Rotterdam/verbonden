package nl.rotterdam.verbonden.core.features.dossier_administration.ui;

import nl.rotterdam.nl_design_system.wicket.components.button.RdButton;
import nl.rotterdam.nl_design_system.wicket.components.form_field_checkbox.RdFormFieldCheckbox;
import nl.rotterdam.verbonden.core.administration_common.RdFormFieldSelect;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.ChangeExtrasDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.RegistratieType;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.TrouwboekjeKeuzeDto;
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
import java.util.stream.Collectors;

public class DossierExtrasUpdatePage extends DossierBeheerBasePage {

    public DossierExtrasUpdatePage(PageParameters params) {
        super(params);
        pageBody.add(
                new Label("titel", "Extra's wijzigen - dossier " + dossierId),
                new BookmarkablePageLink<>("terugLink", DossierDetailPage.class, parametersVoor(dossierId)),
                new FeedbackPanel("feedback"),
                new ChangeExtrasForm("extrasForm")
        );
    }

    private class ChangeExtrasForm extends Form<ExtrasFormDto> {

        ChangeExtrasForm(String id) {
            super(id, Model.of(ExtrasFormDto.vanDto(dossier.extraKeuzes())));
        }

        @Override
        protected void onInitialize() {
            super.onInitialize();
            IModel<ExtrasFormDto> model = getModel();

            Map<Long, String> trouwboekjes = dossierAdministrationService.findTrouwboekjes().stream()
                    .collect(Collectors.toMap(TrouwboekjeKeuzeDto::id, TrouwboekjeKeuzeDto::naam));

            add(
                    new RdFormFieldCheckbox("ringenUitwisselen",
                            LambdaModel.of(model, ExtrasFormDto::isRingenUitwisselen, ExtrasFormDto::setRingenUitwisselen),
                            Model.of("Ringen uitwisselen")),
                    new RdFormFieldCheckbox("muziek",
                            LambdaModel.of(model, ExtrasFormDto::isMuziek, ExtrasFormDto::setMuziek),
                            Model.of("Muziek"))
                            .setVisible(dossier.ceremonieSoort() == CeremonieSoort.GROOT),
                    new RdFormFieldSelect<>("trouwboekje",
                            LambdaModel.of(model, ExtrasFormDto::getTrouwboekjeId, ExtrasFormDto::setTrouwboekjeId),
                            Model.of("Trouwboekje"),
                            List.copyOf(trouwboekjes.keySet()),
                            new LambdaChoiceRenderer<Long>(trouwboekjes::get, String::valueOf))
                            .setNullValid(true),
                    new RdFormFieldCheckbox("internationaleAkte",
                            LambdaModel.of(model, ExtrasFormDto::isInternationaleAkte, ExtrasFormDto::setInternationaleAkte),
                            Model.of("Internationale huwelijksakte"))
                            .setVisible(dossier.registratieType() == RegistratieType.HUWELIJK),
                    new RdButton("opslaan", Model.of("Opslaan"))
            );
        }

        @Override
        protected void onSubmit() {
            ExtrasFormDto f = getModelObject();
            dossierAdministrationService.updateExtras(dossierId, new ChangeExtrasDto(
                    f.isRingenUitwisselen(), f.isMuziek(), f.getTrouwboekjeId(), f.isInternationaleAkte()));
            naarDetailPagina();
        }
    }
}

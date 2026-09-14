package nl.rotterdam.verbonden.core.features.trouwboekje_administration.ui;

import nl.rotterdam.verbonden.core.administration_common.AdministrationBasePage;
import nl.rotterdam.verbonden.core.features.trouwboekje_administration.application.TrouwboekjeAdministrationService;
import nl.rotterdam.verbonden.core.features.trouwboekje_administration.domain.ChangeTrouwboekjeDto;
import nl.rotterdam.nl_design_system.wicket.components.button.RdButton;
import nl.rotterdam.nl_design_system.wicket.components.form_field_text_input.RdFormFieldTextInput;
import nl.rotterdam.nl_design_system.wicket.components.form_field_textarea.RdFormFieldTextArea;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LambdaModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TrouwboekjeUpdatePage extends AdministrationBasePage {

    @SpringBean
    private TrouwboekjeAdministrationService trouwboekjeAdministrationService;

    public TrouwboekjeUpdatePage(PageParameters params) {
        Long id = params.get("id").toOptionalLong();
        if (id == null) {
            setResponsePage(TrouwboekjeAdministrationPage.class);
            return;
        }
        ChangeTrouwboekjeDto dto = trouwboekjeAdministrationService.findById(id).orElse(null);
        if (dto == null) {
            setResponsePage(TrouwboekjeAdministrationPage.class);
            return;
        }

        FeedbackPanel feedback = new FeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);
        pageBody.add(
                new BookmarkablePageLink<>("terugLink", TrouwboekjeAdministrationPage.class),
                feedback,
                new ChangeTrouwboekjeForm("extraForm", dto)
        );
    }

    private class ChangeTrouwboekjeForm extends Form<TrouwboekjeFormDto> {

        private final long trouwboekjeId;

        ChangeTrouwboekjeForm(String id, ChangeTrouwboekjeDto dto) {
            super(id, Model.of(TrouwboekjeFormDto.vanDto(dto)));
            trouwboekjeId = dto.id();
        }

        @Override
        protected void onInitialize() {
            super.onInitialize();
            IModel<TrouwboekjeFormDto> model = getModel();
            add(
                    new RdFormFieldTextInput<>("naam",
                            LambdaModel.of(model, TrouwboekjeFormDto::getNaam, TrouwboekjeFormDto::setNaam),
                            Model.of("Naam")).setRequired(true),
                    new RdFormFieldTextArea<>("omschrijving",
                            LambdaModel.of(model, TrouwboekjeFormDto::getOmschrijving, TrouwboekjeFormDto::setOmschrijving),
                            Model.of("Omschrijving"),
                            Model.of("Korte beschrijving van het trouwboekje")),
                    new RdFormFieldTextInput<>("afbeelding",
                            LambdaModel.of(model, TrouwboekjeFormDto::getAfbeelding, TrouwboekjeFormDto::setAfbeelding),
                            Model.of("Afbeelding URL"),
                            Model.of("URL naar de afbeelding")),
                    new RdFormFieldTextInput<>("prijs",
                            LambdaModel.of(model, TrouwboekjeFormDto::getPrijs, TrouwboekjeFormDto::setPrijs),
                            Model.of("Prijs"),
                            Model.of("Prijs in euro's, bijv. 40.50"))
                            .setModelType(BigDecimal.class),
                    new RdFormFieldTextInput<>("startdatum",
                            LambdaModel.of(model, TrouwboekjeFormDto::getStartdatum, TrouwboekjeFormDto::setStartdatum),
                            Model.of("Startdatum"),
                            Model.of("Datum vanaf wanneer het trouwboekje beschikbaar is")).setHtmlInputType("date").setModelType(LocalDate.class),
                    new RdFormFieldTextInput<>("einddatum",
                            LambdaModel.of(model, TrouwboekjeFormDto::getEinddatum, TrouwboekjeFormDto::setEinddatum),
                            Model.of("Einddatum"),
                            Model.of("Datum tot wanneer het trouwboekje beschikbaar is")).setHtmlInputType("date").setModelType(LocalDate.class),
                    new RdButton("opslaan", Model.of("Opslaan"))
            );
        }

        @Override
        protected void onSubmit() {
            TrouwboekjeFormDto f = getModelObject();
            trouwboekjeAdministrationService.update(new ChangeTrouwboekjeDto(
                    trouwboekjeId,
                    f.getNaam(),
                    f.getOmschrijving(),
                    f.getAfbeelding(),
                    f.getPrijs(),
                    f.getStartdatum(),
                    f.getEinddatum()
            ));
            setResponsePage(TrouwboekjeAdministrationPage.class);
        }
    }
}

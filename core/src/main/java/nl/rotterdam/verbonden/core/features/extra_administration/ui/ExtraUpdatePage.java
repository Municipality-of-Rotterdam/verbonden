package nl.rotterdam.verbonden.core.features.extra_administration.ui;

import nl.rotterdam.verbonden.core.administration_common.AdministrationBasePage;
import nl.rotterdam.verbonden.core.features.extra_administration.application.ExtraAdministrationService;
import nl.rotterdam.verbonden.core.features.extra_administration.domain.ChangeExtraDto;
import nl.rotterdam.verbonden.core.features.extra_administration.domain.ExtraType;
import nl.rotterdam.nl_design_system.wicket.components.button.RdButton;
import nl.rotterdam.nl_design_system.wicket.components.form_field_text_input.RdFormFieldTextInput;
import nl.rotterdam.nl_design_system.wicket.components.form_field_textarea.RdFormFieldTextArea;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.LambdaChoiceRenderer;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LambdaModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ExtraUpdatePage extends AdministrationBasePage {

    @SpringBean
    private ExtraAdministrationService extraAdministrationService;

    public ExtraUpdatePage(PageParameters params) {
        Long id = params.get("id").toOptionalLong();
        if (id == null) {
            setResponsePage(ExtraAdministrationPage.class);
            return;
        }
        ChangeExtraDto dto = extraAdministrationService.findById(id).orElse(null);
        if (dto == null) {
            setResponsePage(ExtraAdministrationPage.class);
            return;
        }

        FeedbackPanel feedback = new FeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);
        pageBody.add(
                new BookmarkablePageLink<>("terugLink", ExtraAdministrationPage.class),
                feedback,
                new ChangeExtraForm("extraForm", dto)
        );
    }

    private class ChangeExtraForm extends Form<ExtraFormDto> {

        private final long extraId;

        ChangeExtraForm(String id, ChangeExtraDto dto) {
            super(id, Model.of(ExtraFormDto.vanDto(dto)));
            extraId = dto.id();
        }

        @Override
        protected void onInitialize() {
            super.onInitialize();
            IModel<ExtraFormDto> model = getModel();
            add(
                    new DropDownChoice<>("type",
                            LambdaModel.of(model, ExtraFormDto::getType, ExtraFormDto::setType),
                            List.of(ExtraType.values()),
                            new LambdaChoiceRenderer<>(ExtraType::getLabel)).setRequired(true),
                    new RdFormFieldTextInput<>("naam",
                            LambdaModel.of(model, ExtraFormDto::getNaam, ExtraFormDto::setNaam),
                            Model.of("Naam")).setRequired(true),
                    new RdFormFieldTextArea<>("omschrijving",
                            LambdaModel.of(model, ExtraFormDto::getOmschrijving, ExtraFormDto::setOmschrijving),
                            Model.of("Omschrijving"),
                            Model.of("Korte beschrijving van de extra")),
                    new RdFormFieldTextInput<>("afbeelding",
                            LambdaModel.of(model, ExtraFormDto::getAfbeelding, ExtraFormDto::setAfbeelding),
                            Model.of("Afbeelding URL"),
                            Model.of("URL naar de afbeelding")),
                    new RdFormFieldTextInput<>("prijs",
                            LambdaModel.of(model, ExtraFormDto::getPrijs, ExtraFormDto::setPrijs),
                            Model.of("Prijs"),
                            Model.of("Prijs in euro's, bijv. 40.50"))
                            .setModelType(BigDecimal.class),
                    new RdFormFieldTextInput<>("startdatum",
                            LambdaModel.of(model, ExtraFormDto::getStartdatum, ExtraFormDto::setStartdatum),
                            Model.of("Startdatum"),
                            Model.of("Datum vanaf wanneer de extra beschikbaar is")).setHtmlInputType("date").setModelType(LocalDate.class),
                    new RdFormFieldTextInput<>("einddatum",
                            LambdaModel.of(model, ExtraFormDto::getEinddatum, ExtraFormDto::setEinddatum),
                            Model.of("Einddatum"),
                            Model.of("Datum tot wanneer de extra beschikbaar is")).setHtmlInputType("date").setModelType(LocalDate.class),
                    new RdButton("opslaan", Model.of("Opslaan"))
            );
        }

        @Override
        protected void onSubmit() {
            ExtraFormDto f = getModelObject();
            extraAdministrationService.update(new ChangeExtraDto(
                    extraId,
                    f.getType(),
                    f.getNaam(),
                    f.getOmschrijving(),
                    f.getAfbeelding(),
                    f.getPrijs(),
                    f.getStartdatum(),
                    f.getEinddatum()
            ));
            setResponsePage(ExtraAdministrationPage.class);
        }
    }
}

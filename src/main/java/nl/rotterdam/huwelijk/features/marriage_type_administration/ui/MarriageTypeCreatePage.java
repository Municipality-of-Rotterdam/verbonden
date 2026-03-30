package nl.rotterdam.huwelijk.features.marriage_type_administration.ui;

import nl.rotterdam.huwelijk.administration_common.AdministrationBasePage;
import nl.rotterdam.huwelijk.features.marriage_type_administration.application.MarriageTypeAdministrationService;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.huwelijk.features.marriage_type_administration.domain.CreateMarriageTypeDto;
import nl.rotterdam.nl_design_system.wicket.components.button.RdButton;
import nl.rotterdam.nl_design_system.wicket.components.form_field_text_input.RdFormFieldTextInput;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LambdaModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.math.BigDecimal;
import java.util.List;

public class MarriageTypeCreatePage extends AdministrationBasePage {

    @SpringBean
    private MarriageTypeAdministrationService marriageTypeAdministrationService;

    public MarriageTypeCreatePage() {
        FeedbackPanel feedback = new FeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);
        pageBody.add(
                new BookmarkablePageLink<>("terugLink", MarriageTypeAdministrationPage.class),
                feedback,
                new CreateMarriageTypeForm("huwelijkstypeForm")
        );
    }

    private class CreateMarriageTypeForm extends Form<MarriageTypeFormDto> {

        CreateMarriageTypeForm(String id) {
            super(id, Model.of(MarriageTypeFormDto.leeg()));
        }

        @Override
        protected void onInitialize() {
            super.onInitialize();
            IModel<MarriageTypeFormDto> model = getModel();
            add(
                    new DropDownChoice<>("soort",
                            LambdaModel.of(model, MarriageTypeFormDto::getSoort, MarriageTypeFormDto::setSoort),
                            List.of(CeremonieSoort.values())).setRequired(true),
                    new RdFormFieldTextInput<>("titel",
                            LambdaModel.of(model, MarriageTypeFormDto::getTitel, MarriageTypeFormDto::setTitel),
                            Model.of("Titel")).setRequired(true),
                    new RdFormFieldTextInput<>("tekst",
                            LambdaModel.of(model, MarriageTypeFormDto::getTekst, MarriageTypeFormDto::setTekst),
                            Model.of("Tekst")).setRequired(true),
                    new RdFormFieldTextInput<>("prijs",
                            LambdaModel.of(model, MarriageTypeFormDto::getPrijs, MarriageTypeFormDto::setPrijs),
                            Model.of("Prijs"),
                            Model.of("Prijs in euro's, bijv. 267.81")).setRequired(true)
                            .setModelType(BigDecimal.class),
                    new RdFormFieldTextInput<>("url",
                            LambdaModel.of(model, MarriageTypeFormDto::getUrl, MarriageTypeFormDto::setUrl),
                            Model.of("URL")).setRequired(true),
                    new RdButton("opslaan", Model.of("Toevoegen"))
            );
        }

        @Override
        protected void onSubmit() {
            MarriageTypeFormDto f = getModelObject();
            marriageTypeAdministrationService.create(new CreateMarriageTypeDto(
                    f.getSoort(),
                    f.getTitel(),
                    f.getTekst(),
                    f.getPrijs(),
                    f.getUrl()
            ));
            setResponsePage(MarriageTypeAdministrationPage.class);
        }
    }
}

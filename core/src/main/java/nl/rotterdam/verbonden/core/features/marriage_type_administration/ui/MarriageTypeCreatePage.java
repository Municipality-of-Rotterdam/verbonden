package nl.rotterdam.verbonden.core.features.marriage_type_administration.ui;

import nl.rotterdam.verbonden.core.administration_common.AdministrationBasePage;
import nl.rotterdam.verbonden.core.administration_common.RdFormFieldSelect;
import nl.rotterdam.verbonden.core.features.location_administration.application.LocationAdministrationService;
import nl.rotterdam.verbonden.core.features.location_administration.domain.ListLocatieDto;

import nl.rotterdam.verbonden.core.features.marriage_type_administration.application.MarriageTypeAdministrationService;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.verbonden.core.features.marriage_type_administration.domain.CreateMarriageTypeDto;
import nl.rotterdam.nl_design_system.wicket.components.button.RdButton;
import nl.rotterdam.nl_design_system.wicket.components.form_field_checkbox.RdFormFieldCheckbox;
import nl.rotterdam.nl_design_system.wicket.components.form_field_text_input.RdFormFieldTextInput;
import nl.rotterdam.nl_design_system.wicket.components.form_field_textarea.RdFormFieldTextArea;
import org.apache.wicket.markup.html.form.*;
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

    @SpringBean
    private LocationAdministrationService locationAdministrationService;

    public MarriageTypeCreatePage() {
        List<ListLocatieDto> alleLocaties = locationAdministrationService.findAllLocaties();

        FeedbackPanel feedback = new FeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);
        pageBody.add(
                new BookmarkablePageLink<>("terugLink", MarriageTypeAdministrationPage.class),
                feedback,
                new CreateMarriageTypeForm("huwelijkstypeForm", alleLocaties)
        );
    }

    private class CreateMarriageTypeForm extends Form<MarriageTypeFormDto> {

        private final List<ListLocatieDto> alleLocaties;

        CreateMarriageTypeForm(String id, List<ListLocatieDto> alleLocaties) {
            super(id, Model.of(MarriageTypeFormDto.leeg()));
            this.alleLocaties = alleLocaties;
        }

        @Override
        protected void onInitialize() {
            super.onInitialize();
            IModel<MarriageTypeFormDto> model = getModel();
            add(
                    new DropDownChoice<>("soort",
                            LambdaModel.of(model, MarriageTypeFormDto::getSoort, MarriageTypeFormDto::setSoort),
                            List.of(CeremonieSoort.values()),
                            new LambdaChoiceRenderer<>(CeremonieSoort::getLabel)).setRequired(true),
                    new RdFormFieldTextInput<>("titel",
                            LambdaModel.of(model, MarriageTypeFormDto::getTitel, MarriageTypeFormDto::setTitel),
                            Model.of("Titel")).setRequired(true),
                    new RdFormFieldTextArea<>("tekst",
                            LambdaModel.of(model, MarriageTypeFormDto::getTekst, MarriageTypeFormDto::setTekst),
                            Model.of("Tekst"),
                            Model.of("Beschrijving van het huwelijkstype")).setRequired(true),
                    new RdFormFieldTextInput<>("prijs",
                            LambdaModel.of(model, MarriageTypeFormDto::getPrijs, MarriageTypeFormDto::setPrijs),
                            Model.of("Prijs"),
                            Model.of("Prijs in euro's, bijv. 267.81")).setRequired(true)
                            .setModelType(BigDecimal.class),
                    new RdFormFieldTextInput<>("url",
                            LambdaModel.of(model, MarriageTypeFormDto::getUrl, MarriageTypeFormDto::setUrl),
                            Model.of("URL")).setRequired(true),
                    new RdFormFieldSelect<>("locatie",
                            LambdaModel.of(model, MarriageTypeFormDto::getLocatie, MarriageTypeFormDto::setLocatie),
                            Model.of("Vaste locatie"),
                            alleLocaties,
                            new LambdaChoiceRenderer<>(ListLocatieDto::naam, l -> Long.toString(l.id())))
                            .setNullValid(true)
                            .setRequired(false),
                    new RdFormFieldCheckbox("active",
                            LambdaModel.of(model, MarriageTypeFormDto::isActive, MarriageTypeFormDto::setActive),
                            Model.of("Actief")),
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
                    f.getUrl(),
                    f.getLocatie() != null ? f.getLocatie().id() : null,
                    f.isActive()
            ));
            setResponsePage(MarriageTypeAdministrationPage.class);
        }
    }
}

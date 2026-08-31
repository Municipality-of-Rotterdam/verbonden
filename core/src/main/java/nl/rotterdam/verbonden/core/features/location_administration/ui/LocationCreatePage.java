package nl.rotterdam.verbonden.core.features.location_administration.ui;

import nl.rotterdam.verbonden.core.administration_common.AdministrationBasePage;
import nl.rotterdam.verbonden.core.features.location_administration.application.LocationAdministrationService;
import nl.rotterdam.verbonden.core.features.location_administration.domain.CreateLocatieDto;
import nl.rotterdam.nl_design_system.wicket.components.button.RdButton;
import nl.rotterdam.nl_design_system.wicket.components.form_field_textarea.RdFormFieldTextArea;
import nl.rotterdam.nl_design_system.wicket.components.form_field_text_input.RdFormFieldTextInput;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LambdaModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

public class LocationCreatePage extends AdministrationBasePage {

    @SpringBean
    private LocationAdministrationService locationAdministrationService;

    public LocationCreatePage() {
        FeedbackPanel feedback = new FeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);
        pageBody.add(
                new BookmarkablePageLink<>("terugLink", LocationAdministrationPage.class),
                feedback,
                new CreateLocatieForm("locatieForm")
        );
    }

    private class CreateLocatieForm extends Form<LocatieFormDto> {

        CreateLocatieForm(String id) {
            super(id, Model.of(LocatieFormDto.leeg()));
        }

        @Override
        protected void onInitialize() {
            super.onInitialize();
            IModel<LocatieFormDto> model = getModel();
            add(
                    new RdFormFieldTextInput<>("naam",
                            LambdaModel.of(model, LocatieFormDto::getNaam, LocatieFormDto::setNaam),
                            Model.of("Naam")).setRequired(true),
                    new RdFormFieldTextInput<>("fotoUrl",
                            LambdaModel.of(model, LocatieFormDto::getFotoUrl, LocatieFormDto::setFotoUrl),
                            Model.of("Foto URL"),
                            Model.of("URL naar de foto van de trouwlocatie")),
                    new RdFormFieldTextArea<>("omschrijving",
                            LambdaModel.of(model, LocatieFormDto::getOmschrijving, LocatieFormDto::setOmschrijving),
                            Model.of("Omschrijving"),
                            Model.of("Beschrijving van de trouwlocatie (HTML toegestaan)")),
                    new RdFormFieldTextInput<>("detailUrl",
                            LambdaModel.of(model, LocatieFormDto::getDetailUrl, LocatieFormDto::setDetailUrl),
                            Model.of("Detail URL"),
                            Model.of("URL naar de detailpagina van de trouwlocatie")),
                    new RdButton("opslaan", Model.of("Toevoegen"))
            );
        }

        @Override
        protected void onSubmit() {
            LocatieFormDto f = getModelObject();
            locationAdministrationService.create(new CreateLocatieDto(
                    f.getNaam(),
                    f.getFotoUrl(),
                    f.getOmschrijving(),
                    f.getDetailUrl()
            ));
            setResponsePage(LocationAdministrationPage.class);
        }
    }
}

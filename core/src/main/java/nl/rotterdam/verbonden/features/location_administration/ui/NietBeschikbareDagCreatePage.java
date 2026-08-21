package nl.rotterdam.verbonden.features.location_administration.ui;

import nl.rotterdam.verbonden.administration_common.AdministrationBasePage;
import nl.rotterdam.verbonden.features.location_administration.application.LocationAdministrationService;
import nl.rotterdam.verbonden.features.location_administration.domain.CreateNietBeschikbareDagDto;
import nl.rotterdam.nl_design_system.wicket.components.button.RdButton;
import nl.rotterdam.nl_design_system.wicket.components.form_field_textarea.RdFormFieldTextArea;
import nl.rotterdam.nl_design_system.wicket.components.form_field_text_input.RdFormFieldTextInput;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LambdaModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.time.LocalDate;

public class NietBeschikbareDagCreatePage extends AdministrationBasePage {

    @SpringBean
    private LocationAdministrationService locationAdministrationService;

    public NietBeschikbareDagCreatePage(PageParameters params) {
        Long locatieId = params.get("locatieId").toOptionalLong();
        if (locatieId == null) {
            setResponsePage(LocationAdministrationPage.class);
            return;
        }

        PageParameters terugParams = new PageParameters();
        terugParams.add("id", locatieId);

        FeedbackPanel feedback = new FeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);
        pageBody.add(
                new BookmarkablePageLink<>("terugLink", LocationUpdatePage.class, terugParams),
                feedback,
                new CreateNietBeschikbareDagForm("nietBeschikbareDagForm", locatieId)
        );
    }

    private class CreateNietBeschikbareDagForm extends Form<NietBeschikbareDagFormDto> {

        private final long locatieId;

        CreateNietBeschikbareDagForm(String id, long locatieId) {
            super(id, Model.of(NietBeschikbareDagFormDto.leeg()));
            this.locatieId = locatieId;
        }

        @Override
        protected void onInitialize() {
            super.onInitialize();
            IModel<NietBeschikbareDagFormDto> model = getModel();
            add(
                    new RdFormFieldTextInput<>("datum",
                            LambdaModel.of(model, NietBeschikbareDagFormDto::getDatum,
                                    NietBeschikbareDagFormDto::setDatum),
                            Model.of("Datum")).setRequired(true).setHtmlInputType("date").setModelType(LocalDate.class),
                    new RdFormFieldTextArea<>("reden",
                            LambdaModel.of(model, NietBeschikbareDagFormDto::getReden,
                                    NietBeschikbareDagFormDto::setReden),
                            Model.of("Reden"),
                            Model.of("Reden waarom de locatie niet beschikbaar is")).setRequired(true),
                    new RdButton("opslaan", Model.of("Toevoegen"))
            );
        }

        @Override
        protected void onSubmit() {
            NietBeschikbareDagFormDto f = getModelObject();
            locationAdministrationService.createNietBeschikbareDag(new CreateNietBeschikbareDagDto(
                    locatieId,
                    f.getDatum(),
                    f.getReden()
            ));
            PageParameters params = new PageParameters();
            params.add("id", locatieId);
            setResponsePage(LocationUpdatePage.class, params);
        }
    }
}

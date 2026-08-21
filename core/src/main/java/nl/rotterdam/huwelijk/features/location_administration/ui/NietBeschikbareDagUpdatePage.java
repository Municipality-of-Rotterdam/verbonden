package nl.rotterdam.huwelijk.features.location_administration.ui;

import nl.rotterdam.huwelijk.administration_common.AdministrationBasePage;
import nl.rotterdam.huwelijk.features.location_administration.application.LocationAdministrationService;
import nl.rotterdam.huwelijk.features.location_administration.domain.ChangeNietBeschikbareDagDto;
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

public class NietBeschikbareDagUpdatePage extends AdministrationBasePage {

    @SpringBean
    private LocationAdministrationService locationAdministrationService;

    public NietBeschikbareDagUpdatePage(PageParameters params) {
        Long locatieId = params.get("locatieId").toOptionalLong();
        Long id = params.get("id").toOptionalLong();

        if (locatieId == null || id == null) {
            setResponsePage(LocationAdministrationPage.class);
            return;
        }

        ChangeNietBeschikbareDagDto dto = locationAdministrationService.findNietBeschikbareDagById(id).orElse(null);
        if (dto == null) {
            PageParameters terugParams = new PageParameters();
            terugParams.add("id", locatieId);
            setResponsePage(LocationUpdatePage.class, terugParams);
            return;
        }

        PageParameters terugParams = new PageParameters();
        terugParams.add("id", locatieId);

        FeedbackPanel feedback = new FeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);
        pageBody.add(
                new BookmarkablePageLink<>("terugLink", LocationUpdatePage.class, terugParams),
                feedback,
                new ChangeNietBeschikbareDagForm("nietBeschikbareDagForm", dto, locatieId)
        );
    }

    private class ChangeNietBeschikbareDagForm extends Form<NietBeschikbareDagFormDto> {

        private final long nietBeschikbareDagId;
        private final long locatieId;

        ChangeNietBeschikbareDagForm(String id, ChangeNietBeschikbareDagDto dto, long locatieId) {
            super(id, Model.of(NietBeschikbareDagFormDto.vanDto(dto)));
            this.nietBeschikbareDagId = dto.id();
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
                    new RdButton("opslaan", Model.of("Opslaan"))
            );
        }

        @Override
        protected void onSubmit() {
            NietBeschikbareDagFormDto f = getModelObject();
            locationAdministrationService.updateNietBeschikbareDag(new ChangeNietBeschikbareDagDto(
                    nietBeschikbareDagId,
                    f.getDatum(),
                    f.getReden()
            ));
            PageParameters params = new PageParameters();
            params.add("id", locatieId);
            setResponsePage(LocationUpdatePage.class, params);
        }
    }
}

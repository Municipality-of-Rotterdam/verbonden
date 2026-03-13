package nl.rotterdam.huwelijk.features.baps_administration.ui;

import nl.rotterdam.huwelijk.beheer_common.BeheerBasePage;
import nl.rotterdam.huwelijk.features.baps_administration.application.BapsAdministrationService;
import nl.rotterdam.huwelijk.features.baps_administration.domain.CreateBapsDto;
import nl.rotterdam.nl_design_system.wicket.components.button.RdButton;
import nl.rotterdam.nl_design_system.wicket.components.form_field_checkbox.RdFormFieldCheckbox;
import nl.rotterdam.nl_design_system.wicket.components.form_field_text_input.RdFormFieldTextInput;
import nl.rotterdam.nl_design_system.wicket.components.form_field_textarea.RdFormFieldTextArea;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LambdaModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BapsToevoegenPage extends BeheerBasePage {

    @SpringBean
    private BapsAdministrationService bapsAdministrationService;

    public BapsToevoegenPage() {
        FeedbackPanel feedback = new FeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);
        add(
                new BookmarkablePageLink<>("terugLink", BapsBeheerPage.class),
                feedback,
                new CreateBapsForm("bapsForm")
        );
    }

    private class CreateBapsForm extends Form<BapsFormDto> {

        private final ListModel<DayOfWeek> beschikbareDagenModel = new ListModel<>(new ArrayList<>());

        CreateBapsForm(String id) {
            super(id, Model.of(BapsFormDto.leeg()));
        }

        @Override
        protected void onInitialize() {
            super.onInitialize();
            IModel<BapsFormDto> model = getModel();
            IModel<Collection<DayOfWeek>> geselecteerdeDagen = LambdaModel.of(
                    model,
                    BapsFormDto::getBeschikbareDagen,
                    (f, v) -> f.setBeschikbareDagen(new ArrayList<>(v)));
            add(
                    new RdFormFieldTextInput<>("naam",
                            LambdaModel.of(model, BapsFormDto::getNaam, BapsFormDto::setNaam),
                            Model.of("Naam")).setRequired(true),
                    new RdFormFieldTextInput<>("fotoUrl",
                            LambdaModel.of(model, BapsFormDto::getFotoUrl, BapsFormDto::setFotoUrl),
                            Model.of("Foto URL"),
                            Model.of("URL naar de profielfoto van de BAPS")),
                    new RdFormFieldTextArea<>("hobbies",
                            LambdaModel.of(model, BapsFormDto::getHobbies, BapsFormDto::setHobbies),
                            Model.of("Hobbies")),
                    new RdFormFieldTextArea<>("beschrijving",
                            LambdaModel.of(model, BapsFormDto::getBeschrijving, BapsFormDto::setBeschrijving),
                            Model.of("Beschrijving")),
                    new DayOfWeekCheckboxGroup("beschikbareDagen", geselecteerdeDagen, Model.of("Beschikbare dagen")),
                    new RdFormFieldCheckbox("actief",
                            LambdaModel.of(model, BapsFormDto::isActief, BapsFormDto::setActief),
                            Model.of("Actief")),
                    new RdFormFieldTextInput<>("actiefVanaf",
                            LambdaModel.of(model, BapsFormDto::getActiefVanaf, BapsFormDto::setActiefVanaf),
                            Model.of("Actief Vanaf"),
                            Model.of("Datum in formaat JJJJ-MM-DD")).setHtmlInputType("date"),
                    new RdFormFieldTextInput<>("actiefTotEnMet",
                            LambdaModel.of(model, BapsFormDto::getActiefTotEnMet, BapsFormDto::setActiefTotEnMet),
                            Model.of("Actief Tot en Met"),
                            Model.of("Datum in formaat JJJJ-MM-DD")).setHtmlInputType("date"),
                    new RdButton("opslaan", Model.of("Toevoegen"))
            );
        }

        @Override
        protected void onSubmit() {
            BapsFormDto f = getModelObject();
            bapsAdministrationService.create(new CreateBapsDto(
                    f.getNaam(),
                    f.getFotoUrl(),
                    f.getHobbies(),
                    f.getBeschrijving(),
                    f.isActief(),
                    f.getActiefVanaf(),
                    f.getActiefTotEnMet(),
                    List.copyOf(beschikbareDagenModel.getObject())
            ));
            setResponsePage(BapsBeheerPage.class);
        }
    }
}

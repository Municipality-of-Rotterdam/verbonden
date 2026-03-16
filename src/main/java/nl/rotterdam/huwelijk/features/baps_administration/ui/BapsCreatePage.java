package nl.rotterdam.huwelijk.features.baps_administration.ui;

import nl.rotterdam.huwelijk.administration_common.AdministrationBasePage;
import nl.rotterdam.huwelijk.features.baps_administration.application.BapsAdministrationService;
import nl.rotterdam.huwelijk.features.baps_administration.domain.CreateBapsDto;
import nl.rotterdam.nl_design_system.wicket.components.button.RdButton;
import nl.rotterdam.nl_design_system.wicket.components.form_field_checkbox.RdFormFieldCheckbox;
import nl.rotterdam.nl_design_system.wicket.components.form_field_text_input.RdFormFieldTextInput;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LambdaModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BapsCreatePage extends AdministrationBasePage {

    @SpringBean
    private BapsAdministrationService bapsAdministrationService;

    public BapsCreatePage() {
        FeedbackPanel feedback = new FeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);
        add(
                new BookmarkablePageLink<>("terugLink", BapsAdministrationPage.class),
                feedback,
                new CreateBapsForm("bapsForm")
        );
    }

    private class CreateBapsForm extends Form<BapsFormDto> {

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
                    new RdFormFieldTextInput<>("detailUrl",
                            LambdaModel.of(model, BapsFormDto::getDetailUrl, BapsFormDto::setDetailUrl),
                            Model.of("Detail URL"),
                            Model.of("URL naar de detailpagina op rotterdam.nl")),
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
                    f.getDetailUrl(),
                    f.isActief(),
                    f.getActiefVanaf(),
                    f.getActiefTotEnMet(),
                    List.copyOf(f.getBeschikbareDagen())
            ));
            setResponsePage(BapsAdministrationPage.class);
        }
    }
}

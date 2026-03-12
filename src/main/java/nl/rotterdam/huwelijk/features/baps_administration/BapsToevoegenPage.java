package nl.rotterdam.huwelijk.features.baps_administration;

import nl.rotterdam.nl_design_system.wicket.components.button.RdButton;
import nl.rotterdam.nl_design_system.wicket.components.form_field_text_input.RdFormFieldTextInput;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.CheckBoxMultipleChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LambdaModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

public class BapsToevoegenPage extends BeheerBasePage {

    @SpringBean
    private BapsAdministrationService bapsAdministrationService;

    public BapsToevoegenPage() {
        add(new BookmarkablePageLink<>("terugLink", BeheerPage.class));

        FeedbackPanel feedback = new FeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);
        add(feedback);

        BapsFormDto formDto = BapsFormDto.leeg();
        Model<BapsFormDto> formDtoModel = Model.of(formDto);
        // TODO this is bad
        ListModel<DayOfWeek> beschikbareDagenModel = new ListModel<>(List.copyOf(formDto.getBeschikbareDagen()));

        Form<BapsFormDto> form = new Form<>("bapsForm", formDtoModel) {
            @Override
            protected void onSubmit() {
                BapsFormDto f = getModelObject();
                CreateBapsDto dto = new CreateBapsDto(
                        f.getNaam(),
                        f.getFotoUrl(),
                        f.getHobbies(),
                        f.getBeschrijving(),
                        f.isActief(),
                        parseDate(f.getActiefVanaf()),
                        parseDate(f.getActiefTotEnMet()),
                        List.copyOf(beschikbareDagenModel.getObject())
                );
                bapsAdministrationService.create(dto);
                setResponsePage(BeheerPage.class);
            }
        };

        form.add(new RdFormFieldTextInput<>("naam",
                LambdaModel.of(formDtoModel, BapsFormDto::getNaam, BapsFormDto::setNaam),
                Model.of("Naam")).setRequired(true));

        form.add(new RdFormFieldTextInput<>("fotoUrl",
                LambdaModel.of(formDtoModel, BapsFormDto::getFotoUrl, BapsFormDto::setFotoUrl),
                Model.of("Foto URL"),
                Model.of("URL naar de profielfoto van de BAPS")));

        form.add(new Label("hobbiesLabel", Model.of("Hobbies")));
        form.add(new TextArea<>("hobbies",
                LambdaModel.of(formDtoModel, BapsFormDto::getHobbies, BapsFormDto::setHobbies)));

        form.add(new Label("beschrijvingLabel", Model.of("Beschrijving")));
        form.add(new TextArea<>("beschrijving",
                LambdaModel.of(formDtoModel, BapsFormDto::getBeschrijving, BapsFormDto::setBeschrijving)));

        form.add(new Label("beschikbareDagenLabel", Model.of("Beschikbare Dagen")));
        form.add(new CheckBoxMultipleChoice<>("beschikbareDagen",
                beschikbareDagenModel,
                List.of(DayOfWeek.values()),
                dagRenderer()));

        form.add(new Label("actiefLabel", Model.of("Actief")));
        form.add(new CheckBox("actief",
                LambdaModel.of(formDtoModel, BapsFormDto::isActief, BapsFormDto::setActief)));

        form.add(new RdFormFieldTextInput<>("actiefVanaf",
                LambdaModel.of(formDtoModel, BapsFormDto::getActiefVanaf, BapsFormDto::setActiefVanaf),
                Model.of("Actief Vanaf"),
                Model.of("Datum in formaat JJJJ-MM-DD")).setInputType("date"));

        form.add(new RdFormFieldTextInput<>("actiefTotEnMet",
                LambdaModel.of(formDtoModel, BapsFormDto::getActiefTotEnMet, BapsFormDto::setActiefTotEnMet),
                Model.of("Actief Tot en Met"),
                Model.of("Datum in formaat JJJJ-MM-DD")).setInputType("date"));

        form.add(new RdButton("opslaan", Model.of("Toevoegen")));

        add(form);
    }

    static IChoiceRenderer<DayOfWeek> dagRenderer() {
        return new IChoiceRenderer<>() {
            @Override
            public Object getDisplayValue(DayOfWeek day) {
                return day.getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            }

            @Override
            public String getIdValue(DayOfWeek day, int index) {
                return day.name();
            }

            @Override
            public DayOfWeek getObject(String id,
                    IModel<? extends List<? extends DayOfWeek>> choices) {
                return DayOfWeek.valueOf(id);
            }
        };
    }
}

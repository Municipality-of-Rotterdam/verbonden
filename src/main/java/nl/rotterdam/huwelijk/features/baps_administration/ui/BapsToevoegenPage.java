package nl.rotterdam.huwelijk.features.baps_administration.ui;

import nl.rotterdam.huwelijk.beheer_common.BeheerBasePage;
import nl.rotterdam.huwelijk.features.baps_administration.application.BapsAdministrationService;
import nl.rotterdam.huwelijk.features.baps_administration.domain.CreateBapsDto;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BapsToevoegenPage extends BeheerBasePage {

    @SpringBean
    private BapsAdministrationService bapsAdministrationService;

    public BapsToevoegenPage() {
        FeedbackPanel feedback = new FeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);
        add(
                new BookmarkablePageLink<>("terugLink", BeheerPage.class),
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
            add(
                    new RdFormFieldTextInput<>("naam",
                            LambdaModel.of(model, f -> f.naam, (f, v) -> f.naam = v),
                            Model.of("Naam")).setRequired(true),
                    new RdFormFieldTextInput<>("fotoUrl",
                            LambdaModel.of(model, f -> f.fotoUrl, (f, v) -> f.fotoUrl = v),
                            Model.of("Foto URL"),
                            Model.of("URL naar de profielfoto van de BAPS")),
                    new Label("hobbiesLabel", Model.of("Hobbies")),
                    new TextArea<>("hobbies",
                            LambdaModel.of(model, f -> f.hobbies, (f, v) -> f.hobbies = v)),
                    new Label("beschrijvingLabel", Model.of("Beschrijving")),
                    new TextArea<>("beschrijving",
                            LambdaModel.of(model, f -> f.beschrijving, (f, v) -> f.beschrijving = v)),
                    new Label("beschikbareDagenLabel", Model.of("Beschikbare Dagen")),
                    new CheckBoxMultipleChoice<>("beschikbareDagen",
                            beschikbareDagenModel,
                            List.of(DayOfWeek.values()),
                            dagRenderer()),
                    new Label("actiefLabel", Model.of("Actief")),
                    new CheckBox("actief",
                            LambdaModel.of(model, f -> f.actief, (f, v) -> f.actief = v)),
                    new RdFormFieldTextInput<>("actiefVanaf",
                            LambdaModel.of(model, f -> f.actiefVanaf, (f, v) -> f.actiefVanaf = v),
                            Model.of("Actief Vanaf"),
                            Model.of("Datum in formaat JJJJ-MM-DD")).setInputType("date"),
                    new RdFormFieldTextInput<>("actiefTotEnMet",
                            LambdaModel.of(model, f -> f.actiefTotEnMet, (f, v) -> f.actiefTotEnMet = v),
                            Model.of("Actief Tot en Met"),
                            Model.of("Datum in formaat JJJJ-MM-DD")).setInputType("date"),
                    new RdButton("opslaan", Model.of("Toevoegen"))
            );
        }

        @Override
        protected void onSubmit() {
            BapsFormDto f = getModelObject();
            bapsAdministrationService.create(new CreateBapsDto(
                    f.naam,
                    f.fotoUrl,
                    f.hobbies,
                    f.beschrijving,
                    f.actief,
                    f.actiefVanaf,
                    f.actiefTotEnMet,
                    List.copyOf(beschikbareDagenModel.getObject())
            ));
            setResponsePage(BeheerPage.class);
        }
    }

    private static IChoiceRenderer<DayOfWeek> dagRenderer() {
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

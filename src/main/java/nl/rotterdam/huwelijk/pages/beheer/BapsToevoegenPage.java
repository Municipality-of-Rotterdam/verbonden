package nl.rotterdam.huwelijk.pages.beheer;

import nl.rotterdam.huwelijk.baps.BapsDto;
import nl.rotterdam.huwelijk.baps.BapsService;
import nl.rotterdam.nl_design_system.wicket.components.button.RdButton;
import nl.rotterdam.nl_design_system.wicket.components.form_field_text_input.RdFormFieldTextInput;
import nl.rotterdam.huwelijk.pages.BasePage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.io.Serial;

public class BapsToevoegenPage extends BasePage {

    @Serial
    private static final long serialVersionUID = 1L;

    @SpringBean
    private BapsService bapsService;

    public BapsToevoegenPage() {
        add(new BookmarkablePageLink<>("terugLink", BeheerPage.class));

        FeedbackPanel feedback = new FeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);
        add(feedback);

        Model<String> naamModel = Model.of("");
        Model<String> fotoUrlModel = Model.of("");
        Model<String> hobbiesModel = Model.of("");
        Model<String> beschrijvingModel = Model.of("");
        Model<String> beschikbareDagenModel = Model.of("");
        Model<Boolean> actiefModel = Model.of(true);
        Model<String> actiefVanafModel = Model.of("");
        Model<String> actiefTotEnMetModel = Model.of("");

        Form<Void> form = new Form<>("bapsForm") {
            @Serial
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit() {
                BapsDto dto = new BapsDto(
                        null,
                        naamModel.getObject(),
                        fotoUrlModel.getObject(),
                        hobbiesModel.getObject(),
                        beschrijvingModel.getObject(),
                        Boolean.TRUE.equals(actiefModel.getObject()),
                        parseDate(actiefVanafModel.getObject()),
                        parseDate(actiefTotEnMetModel.getObject()),
                        beschikbareDagenModel.getObject(),
                        null
                );
                bapsService.save(dto);
                setResponsePage(BeheerPage.class);
            }
        };

        form.add(new RdFormFieldTextInput<String>("naam", naamModel,
                Model.of("Naam")).setRequired(true));

        form.add(new RdFormFieldTextInput<String>("fotoUrl", fotoUrlModel,
                Model.of("Foto URL"),
                Model.of("URL naar de profielfoto van de BAPS")));

        form.add(new Label("hobbiesLabel", Model.of("Hobbies")));
        form.add(new TextArea<>("hobbies", hobbiesModel));

        form.add(new Label("beschrijvingLabel", Model.of("Beschrijving")));
        form.add(new TextArea<>("beschrijving", beschrijvingModel));

        form.add(new RdFormFieldTextInput<String>("beschikbareDagen", beschikbareDagenModel,
                Model.of("Beschikbare Dagen"),
                Model.of("Bijv. Maandag, Woensdag, Vrijdag")));

        form.add(new Label("actiefLabel", Model.of("Actief")));
        form.add(new CheckBox("actief", actiefModel));

        form.add(new RdFormFieldTextInput<String>("actiefVanaf", actiefVanafModel,
                Model.of("Actief Vanaf"),
                Model.of("Datum in formaat JJJJ-MM-DD")).setInputType("date"));

        form.add(new RdFormFieldTextInput<String>("actiefTotEnMet", actiefTotEnMetModel,
                Model.of("Actief Tot en Met"),
                Model.of("Datum in formaat JJJJ-MM-DD")).setInputType("date"));

        form.add(new RdButton("opslaan", Model.of("Toevoegen")));

        add(form);
    }
}

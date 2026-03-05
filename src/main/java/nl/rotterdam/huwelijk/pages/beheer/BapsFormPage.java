package nl.rotterdam.huwelijk.pages.beheer;

import nl.rotterdam.huwelijk.baps.Baps;
import nl.rotterdam.huwelijk.baps.BapsService;
import nl.rotterdam.nl_design_system.wicket.components.button.RdButton;
import nl.rotterdam.nl_design_system.wicket.components.form_field_text_input.RdFormFieldTextInput;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LambdaModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.io.Serial;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class BapsFormPage extends WebPage {

    @Serial
    private static final long serialVersionUID = 1L;

    @SpringBean
    private BapsService bapsService;

    public BapsFormPage(PageParameters params) {
        Long id = params.get("id").toOptionalLong();
        Baps baps = (id != null)
                ? bapsService.findById(id).orElseGet(Baps::new)
                : new Baps();

        boolean isNieuw = baps.getId() == null;

        add(new Label("paginaTitel",
                Model.of(isNieuw ? "Nieuwe BAPS toevoegen" : "BAPS bewerken")));
        add(new BookmarkablePageLink<>("terugLink", BeheerPage.class));

        FeedbackPanel feedback = new FeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);
        add(feedback);

        Form<Void> form = new Form<>("bapsForm") {
            @Serial
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit() {
                bapsService.save(baps);
                setResponsePage(BeheerPage.class);
            }
        };

        form.add(new RdFormFieldTextInput<String>("naam",
                LambdaModel.of(baps::getNaam, baps::setNaam),
                Model.of("Naam")).setRequired(true));

        form.add(new RdFormFieldTextInput<String>("fotoUrl",
                LambdaModel.of(baps::getFotoUrl, baps::setFotoUrl),
                Model.of("Foto URL"),
                Model.of("URL naar de profielfoto van de BAPS")));

        form.add(new Label("hobbiesLabel", Model.of("Hobbies")));
        form.add(new TextArea<String>("hobbies", LambdaModel.of(baps::getHobbies, baps::setHobbies)));

        form.add(new Label("beschrijvingLabel", Model.of("Beschrijving")));
        form.add(new TextArea<String>("beschrijving",
                LambdaModel.of(baps::getBeschrijving, baps::setBeschrijving)));

        form.add(new RdFormFieldTextInput<String>("beschikbareDagen",
                LambdaModel.of(baps::getBeschikbareDagen, baps::setBeschikbareDagen),
                Model.of("Beschikbare Dagen"),
                Model.of("Bijv. Maandag, Woensdag, Vrijdag")));

        form.add(new Label("actiefLabel", Model.of("Actief")));
        form.add(new CheckBox("actief", LambdaModel.of(baps::isActief, baps::setActief)));

        form.add(new RdFormFieldTextInput<String>("actiefVanaf",
                LambdaModel.of(
                        () -> baps.getActiefVanaf() != null ? baps.getActiefVanaf().toString() : "",
                        s -> baps.setActiefVanaf(parseDate(s))),
                Model.of("Actief Vanaf"),
                Model.of("Datum in formaat JJJJ-MM-DD")).setInputType("date"));

        form.add(new RdFormFieldTextInput<String>("actiefTotEnMet",
                LambdaModel.of(
                        () -> baps.getActiefTotEnMet() != null ? baps.getActiefTotEnMet().toString() : "",
                        s -> baps.setActiefTotEnMet(parseDate(s))),
                Model.of("Actief Tot en Met"),
                Model.of("Datum in formaat JJJJ-MM-DD")).setInputType("date"));

        form.add(new RdButton("opslaan",
                Model.of(isNieuw ? "Toevoegen" : "Opslaan")));

        add(form);
    }

    /** Parses an ISO date string (yyyy-MM-dd); returns {@code null} on blank or invalid input. */
    private static LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}

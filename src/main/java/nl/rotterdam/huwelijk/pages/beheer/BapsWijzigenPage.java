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
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.io.Serial;
import java.time.LocalDateTime;

public class BapsWijzigenPage extends BasePage {

    @Serial
    private static final long serialVersionUID = 1L;

    @SpringBean
    private BapsService bapsService;

    public BapsWijzigenPage(PageParameters params) {
        Long id = params.get("id").toOptionalLong();
        if (id == null) {
            setResponsePage(BeheerPage.class);
            return;
        }
        BapsDto dto = bapsService.findById(id).orElse(null);
        if (dto == null) {
            setResponsePage(BeheerPage.class);
            return;
        }

        Long dtoId = dto.id();
        LocalDateTime aangemaaktOp = dto.aangemaaktOp();

        add(new BookmarkablePageLink<>("terugLink", BeheerPage.class));

        FeedbackPanel feedback = new FeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);
        add(feedback);

        Model<String> naamModel = Model.of(dto.naam() != null ? dto.naam() : "");
        Model<String> fotoUrlModel = Model.of(dto.fotoUrl() != null ? dto.fotoUrl() : "");
        Model<String> hobbiesModel = Model.of(dto.hobbies() != null ? dto.hobbies() : "");
        Model<String> beschrijvingModel = Model.of(dto.beschrijving() != null ? dto.beschrijving() : "");
        Model<String> beschikbareDagenModel = Model.of(dto.beschikbareDagen() != null ? dto.beschikbareDagen() : "");
        Model<Boolean> actiefModel = Model.of(dto.actief());
        Model<String> actiefVanafModel = Model.of(dto.actiefVanaf() != null ? dto.actiefVanaf().toString() : "");
        Model<String> actiefTotEnMetModel = Model.of(dto.actiefTotEnMet() != null ? dto.actiefTotEnMet().toString() : "");

        Form<Void> form = new Form<>("bapsForm") {
            @Serial
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit() {
                BapsDto saveDto = new BapsDto(
                        dtoId,
                        naamModel.getObject(),
                        fotoUrlModel.getObject(),
                        hobbiesModel.getObject(),
                        beschrijvingModel.getObject(),
                        Boolean.TRUE.equals(actiefModel.getObject()),
                        parseDate(actiefVanafModel.getObject()),
                        parseDate(actiefTotEnMetModel.getObject()),
                        beschikbareDagenModel.getObject(),
                        aangemaaktOp
                );
                bapsService.save(saveDto);
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

        form.add(new RdButton("opslaan", Model.of("Opslaan")));

        add(form);
    }
}

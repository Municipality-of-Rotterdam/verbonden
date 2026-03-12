package nl.rotterdam.huwelijk.features.baps_administration.ui;

import nl.rotterdam.huwelijk.beheer_common.BeheerBasePage;
import nl.rotterdam.huwelijk.features.baps_administration.application.BapsAdministrationService;
import nl.rotterdam.huwelijk.features.baps_administration.domain.ChangeBapsDto;
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
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BapsWijzigenPage extends BeheerBasePage {

    @SpringBean
    private BapsAdministrationService bapsAdministrationService;

    public BapsWijzigenPage(PageParameters params) {
        Long id = params.get("id").toOptionalLong();
        if (id == null) {
            setResponsePage(BeheerPage.class);
            return;
        }
        ChangeBapsDto dto = bapsAdministrationService.findById(id).orElse(null);
        if (dto == null) {
            setResponsePage(BeheerPage.class);
            return;
        }

        Long dtoId = dto.id();

        add(new BookmarkablePageLink<>("terugLink", BeheerPage.class));

        FeedbackPanel feedback = new FeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);
        add(feedback);

        BapsFormDto formDto = BapsFormDto.vanDto(dto);
        Model<BapsFormDto> formDtoModel = Model.of(formDto);
        IModel<Collection<DayOfWeek>> geselecteerdeDagen = LambdaModel.of(
                formDtoModel,
                f -> f.beschikbareDagen,
                (f, v) -> f.beschikbareDagen = new ArrayList<>(v));

        Form<BapsFormDto> form = new Form<>("bapsForm", formDtoModel) {
            @Override
            protected void onSubmit() {
                BapsFormDto f = getModelObject();
                ChangeBapsDto saveDto = new ChangeBapsDto(
                        dtoId,
                        f.naam,
                        f.fotoUrl,
                        f.hobbies,
                        f.beschrijving,
                        f.actief,
                        f.actiefVanaf,
                        f.actiefTotEnMet,
                        List.copyOf(geselecteerdeDagen.getObject())
                );
                bapsAdministrationService.update(saveDto);
                setResponsePage(BeheerPage.class);
            }
        };

        form.add(new RdFormFieldTextInput<>("naam",
                LambdaModel.of(formDtoModel, f -> f.naam, (f, v) -> f.naam = v),
                Model.of("Naam")).setRequired(true));

        form.add(new RdFormFieldTextInput<>("fotoUrl",
                LambdaModel.of(formDtoModel, f -> f.fotoUrl, (f, v) -> f.fotoUrl = v),
                Model.of("Foto URL"),
                Model.of("URL naar de profielfoto van de BAPS")));

        form.add(new RdFormFieldTextArea<>("hobbies",
                        LambdaModel.of(formDtoModel, f -> f.hobbies, (f, v) -> f.hobbies = v),
                        Model.of("Hobbies")
                ),
                new RdFormFieldTextArea<>("beschrijving",
                        LambdaModel.of(formDtoModel, f -> f.beschrijving, (f, v) -> f.beschrijving = v),
                        Model.of("Beschrijving")
                )
        );

        form.add(new DayOfWeekCheckboxGroup("beschikbareDagen", geselecteerdeDagen, Model.of("Beschikbare dagen") ));

        form.add(new RdFormFieldCheckbox("actief",
                LambdaModel.of(formDtoModel, f -> f.actief, (f, v) -> f.actief = v),
                Model.of("Actief")));

        form.add(new RdFormFieldTextInput<>("actiefVanaf",
                LambdaModel.of(formDtoModel, f -> f.actiefVanaf, (f, v) -> f.actiefVanaf = v),
                Model.of("Actief Vanaf"),
                Model.of("Datum in formaat JJJJ-MM-DD")).setInputType("date"));

        form.add(new RdFormFieldTextInput<>("actiefTotEnMet",
                LambdaModel.of(formDtoModel, f -> f.actiefTotEnMet, (f, v) -> f.actiefTotEnMet = v),
                Model.of("Actief Tot en Met"),
                Model.of("Datum in formaat JJJJ-MM-DD")).setInputType("date"));

        form.add(new RdButton("opslaan", Model.of("Opslaan")));

        add(form);
    }
}

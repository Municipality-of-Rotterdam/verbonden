package nl.rotterdam.huwelijk.features.baps_administration;

import nl.rotterdam.huwelijk.beheer_common.BeheerBasePage;
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
        IModel<Collection<DayOfWeek>> geselecteerdeDagen = LambdaModel.of(formDtoModel, BapsFormDto::getBeschikbareDagen, BapsFormDto::setBeschikbareDagen);

        Form<BapsFormDto> form = new Form<>("bapsForm", formDtoModel) {
            @Override
            protected void onSubmit() {
                BapsFormDto f = getModelObject();
                ChangeBapsDto saveDto = new ChangeBapsDto(
                        dtoId,
                        f.getNaam(),
                        f.getFotoUrl(),
                        f.getHobbies(),
                        f.getBeschrijving(),
                        f.isActief(),
                        parseDate(f.getActiefVanaf()),
                        parseDate(f.getActiefTotEnMet()),
                        List.copyOf(geselecteerdeDagen.getObject())
                );
                bapsAdministrationService.update(saveDto);
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

        form.add(new RdFormFieldTextArea<>("hobbies",
                        LambdaModel.of(formDtoModel, BapsFormDto::getHobbies, BapsFormDto::setHobbies),
                        Model.of("Hobbies")
                ),
                new RdFormFieldTextArea<>("beschrijving",
                        LambdaModel.of(formDtoModel, BapsFormDto::getBeschrijving, BapsFormDto::setBeschrijving),
                        Model.of("Beschrijving")
                )
        );

        form.add(new DayOfWeekCheckboxGroup("beschikbareDagen", geselecteerdeDagen, Model.of("Beschikbare dagen") ));

        form.add(new RdFormFieldCheckbox("actief",
                LambdaModel.of(formDtoModel, BapsFormDto::isActief, BapsFormDto::setActief),
                Model.of("Actief")));

        form.add(new RdFormFieldTextInput<>("actiefVanaf",
                LambdaModel.of(formDtoModel, BapsFormDto::getActiefVanaf, BapsFormDto::setActiefVanaf),
                Model.of("Actief Vanaf"),
                Model.of("Datum in formaat JJJJ-MM-DD")).setInputType("date"));

        form.add(new RdFormFieldTextInput<>("actiefTotEnMet",
                LambdaModel.of(formDtoModel, BapsFormDto::getActiefTotEnMet, BapsFormDto::setActiefTotEnMet),
                Model.of("Actief Tot en Met"),
                Model.of("Datum in formaat JJJJ-MM-DD")).setInputType("date"));

        form.add(new RdButton("opslaan", Model.of("Opslaan")));

        add(form);
    }
}

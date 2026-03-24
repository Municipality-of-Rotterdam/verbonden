package nl.rotterdam.huwelijk.features.babs_administration.ui;

import nl.rotterdam.huwelijk.administration_common.AdministrationBasePage;
import nl.rotterdam.huwelijk.features.babs_administration.application.BabsAdministrationService;
import nl.rotterdam.huwelijk.features.babs_administration.domain.ChangeBabsDto;
import nl.rotterdam.nl_design_system.wicket.components.button.RdButton;
import nl.rotterdam.nl_design_system.wicket.components.form_field_checkbox.RdFormFieldCheckbox;
import nl.rotterdam.nl_design_system.wicket.components.form_field_text_input.RdFormFieldTextInput;
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

public class BabsUpdatePage extends AdministrationBasePage {

    @SpringBean
    private BabsAdministrationService babsAdministrationService;

    public BabsUpdatePage(PageParameters params) {
        Long id = params.get("id").toOptionalLong();
        if (id == null) {
            setResponsePage(BabsAdministrationPage.class);
            return;
        }
        ChangeBabsDto dto = babsAdministrationService.findById(id).orElse(null);
        if (dto == null) {
            setResponsePage(BabsAdministrationPage.class);
            return;
        }

        FeedbackPanel feedback = new FeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);
        pageBody.add(
                new BookmarkablePageLink<>("terugLink", BabsAdministrationPage.class),
                feedback,
                new ChangeBabsForm("babsForm", dto)
        );
    }

    private class ChangeBabsForm extends Form<BabsFormDto> {

        private final long babsId;

        ChangeBabsForm(String id, ChangeBabsDto dto) {
            super(id, Model.of(BabsFormDto.vanDto(dto)));
            babsId = dto.id();
        }

        @Override
        protected void onInitialize() {
            super.onInitialize();
            IModel<BabsFormDto> model = getModel();
            IModel<Collection<DayOfWeek>> geselecteerdeDagen = LambdaModel.of(
                    model,
                    BabsFormDto::getBeschikbareDagen,
                    (f, v) -> f.setBeschikbareDagen(new ArrayList<>(v)));
            add(
                    new RdFormFieldTextInput<>("naam",
                            LambdaModel.of(model, BabsFormDto::getNaam, BabsFormDto::setNaam),
                            Model.of("Naam")).setRequired(true),
                    new RdFormFieldTextInput<>("fotoUrl",
                            LambdaModel.of(model, BabsFormDto::getFotoUrl, BabsFormDto::setFotoUrl),
                            Model.of("Foto URL"),
                            Model.of("URL naar de profielfoto van de BABS")),
                    new RdFormFieldTextInput<>("detailUrl",
                            LambdaModel.of(model, BabsFormDto::getDetailUrl, BabsFormDto::setDetailUrl),
                            Model.of("Detail URL"),
                            Model.of("URL naar de detailpagina op rotterdam.nl")),
                    new DayOfWeekCheckboxGroup("beschikbareDagen", geselecteerdeDagen, Model.of("Beschikbare dagen")),
                    new RdFormFieldCheckbox("actief",
                            LambdaModel.of(model, BabsFormDto::isActief, BabsFormDto::setActief),
                            Model.of("Actief")),
                    new RdFormFieldTextInput<>("actiefVanaf",
                            LambdaModel.of(model, BabsFormDto::getActiefVanaf, BabsFormDto::setActiefVanaf),
                            Model.of("Actief Vanaf"),
                            Model.of("Datum in formaat JJJJ-MM-DD"))
                            .setHtmlInputType("date"),
                    new RdFormFieldTextInput<>("actiefTotEnMet",
                            LambdaModel.of(model, BabsFormDto::getActiefTotEnMet, BabsFormDto::setActiefTotEnMet),
                            Model.of("Actief Tot en Met"),
                            Model.of("Datum in formaat JJJJ-MM-DD"))
                            .setHtmlInputType("date"),
                    new RdButton("opslaan", Model.of("Opslaan"))
            );
        }

        @Override
        protected void onSubmit() {
            BabsFormDto f = getModelObject();
            babsAdministrationService.update(new ChangeBabsDto(
                    babsId,
                    f.getNaam(),
                    f.getFotoUrl(),
                    f.getDetailUrl(),
                    f.isActief(),
                    f.getActiefVanaf(),
                    f.getActiefTotEnMet(),
                    List.copyOf(f.getBeschikbareDagen())
            ));
            setResponsePage(BabsAdministrationPage.class);
        }
    }
}

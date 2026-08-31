package nl.rotterdam.verbonden.core.features.location_administration.ui;

import nl.rotterdam.verbonden.core.administration_common.AdministrationBasePage;
import nl.rotterdam.verbonden.core.administration_common.RdFormFieldSelect;
import nl.rotterdam.verbonden.core.features.location_administration.application.LocationAdministrationService;
import nl.rotterdam.verbonden.core.features.location_administration.domain.CreateBeschikbaarheidDto;
import nl.rotterdam.verbonden.core.features.location_administration.domain.HuwelijksType;
import nl.rotterdam.nl_design_system.wicket.components.button.RdButton;
import nl.rotterdam.nl_design_system.wicket.components.form_field_text_input.RdFormFieldTextInput;
import org.apache.wicket.markup.html.form.EnumChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LambdaModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Arrays;

public class BeschikbaarheidCreatePage extends AdministrationBasePage {

    @SpringBean
    private LocationAdministrationService locationAdministrationService;

    public BeschikbaarheidCreatePage(PageParameters params) {
        Long locatieId = params.get("locatieId").toOptionalLong();
        if (locatieId == null) {
            setResponsePage(LocationAdministrationPage.class);
            return;
        }

        PageParameters terugParams = new PageParameters();
        terugParams.add("id", locatieId);

        FeedbackPanel feedback = new FeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);
        pageBody.add(
                new BookmarkablePageLink<>("terugLink", LocationUpdatePage.class, terugParams),
                feedback,
                new CreateBeschikbaarheidForm("beschikbaarheidForm", locatieId)
        );
    }

    private class CreateBeschikbaarheidForm extends Form<BeschikbaarheidFormDto> {

        private final long locatieId;

        CreateBeschikbaarheidForm(String id, long locatieId) {
            super(id, Model.of(BeschikbaarheidFormDto.leeg()));
            this.locatieId = locatieId;
        }

        @Override
        protected void onInitialize() {
            super.onInitialize();
            IModel<BeschikbaarheidFormDto> model = getModel();
            add(
                    new RdFormFieldSelect<>("huwelijkstype",
                            LambdaModel.of(model, BeschikbaarheidFormDto::getHuwelijkstype,
                                    BeschikbaarheidFormDto::setHuwelijkstype),
                            Model.of("Huwelijkstype"),
                            Arrays.asList(HuwelijksType.values()),
                            new EnumChoiceRenderer<>(this))
                            .setRequired(true),
                    new RdFormFieldSelect<>("dagVanDeWeek",
                            LambdaModel.of(model, BeschikbaarheidFormDto::getDagVanDeWeek,
                                    BeschikbaarheidFormDto::setDagVanDeWeek),
                            Model.of("Dag van de week"),
                            Arrays.asList(DayOfWeek.values()),
                            new DayOfWeekChoiceRenderer())
                            .setRequired(true),
                    new RdFormFieldTextInput<>("startTijd",
                            LambdaModel.of(model, BeschikbaarheidFormDto::getStartTijd,
                                    BeschikbaarheidFormDto::setStartTijd),
                            Model.of("Starttijd")).setRequired(true).setHtmlInputType("time").setModelType(LocalTime.class),
                    new RdFormFieldTextInput<>("eindTijd",
                            LambdaModel.of(model, BeschikbaarheidFormDto::getEindTijd,
                                    BeschikbaarheidFormDto::setEindTijd),
                            Model.of("Eindtijd")).setRequired(true).setHtmlInputType("time").setModelType(LocalTime.class),
                    new RdFormFieldTextInput<>("duurInMinuten",
                            LambdaModel.of(model, BeschikbaarheidFormDto::getDuurInMinuten,
                                    BeschikbaarheidFormDto::setDuurInMinuten),
                            Model.of("Duur (minuten)")).setRequired(true).setHtmlInputType("number"),
                    new RdFormFieldTextInput<>("prijs",
                            LambdaModel.of(model, BeschikbaarheidFormDto::getPrijs,
                                    BeschikbaarheidFormDto::setPrijs),
                            Model.of("Prijs (euro)")).setRequired(true).setHtmlInputType("number"),
                    new RdFormFieldTextInput<>("ingangsdatum",
                            LambdaModel.of(model, BeschikbaarheidFormDto::getIngangsdatum,
                                    BeschikbaarheidFormDto::setIngangsdatum),
                            Model.of("Ingangsdatum")).setRequired(true).setHtmlInputType("date"),
                    new RdFormFieldTextInput<>("einddatum",
                            LambdaModel.of(model, BeschikbaarheidFormDto::getEinddatum,
                                    BeschikbaarheidFormDto::setEinddatum),
                            Model.of("Einddatum")).setRequired(true).setHtmlInputType("date"),
                    new RdButton("opslaan", Model.of("Toevoegen"))
            );
        }

        @Override
        protected void onSubmit() {
            BeschikbaarheidFormDto f = getModelObject();
            locationAdministrationService.createBeschikbaarheid(new CreateBeschikbaarheidDto(
                    locatieId,
                    f.getHuwelijkstype(),
                    f.getDagVanDeWeek(),
                    f.getStartTijd(),
                    f.getEindTijd(),
                    f.getDuurInMinuten(),
                    f.getPrijs() != null ? f.getPrijs() : BigDecimal.ZERO,
                    f.getIngangsdatum(),
                    f.getEinddatum()
            ));
            PageParameters params = new PageParameters();
            params.add("id", locatieId);
            setResponsePage(LocationUpdatePage.class, params);
        }
    }
}

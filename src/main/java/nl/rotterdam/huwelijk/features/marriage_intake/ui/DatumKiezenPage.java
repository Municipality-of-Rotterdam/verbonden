package nl.rotterdam.huwelijk.features.marriage_intake.ui;

import nl.rotterdam.huwelijk.burger_common.BurgerBasePage;
import nl.rotterdam.huwelijk.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.nl_design_system.wicket.components.breadcrumb_nav.RdBreadcrumbNavPanel;
import nl.rotterdam.nl_design_system.wicket.components.breadcrumb_nav.RdBreadcrumbNavRecord;
import nl.rotterdam.nl_design_system.wicket.components.button.RdButton;
import nl.rotterdam.nl_design_system.wicket.components.date_picker.RdDatePicker;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static nl.rotterdam.huwelijk.features.marriage_intake.ui.MarriageIntakeHeaderItems.MARRIAGE_INTAKE_CSS;

public class DatumKiezenPage extends BurgerBasePage {

    @SpringBean
    private MarriageIntakeService marriageIntakeService;

    private final UUID dossierId;

    @Override
    protected IModel<String> getTitleModel() {
        return new ResourceModel("datum.kiezen.page.title");
    }

    public DatumKiezenPage(PageParameters params) {
        this.dossierId = UUID.fromString(params.get("dossierId").toString());
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        List<RdBreadcrumbNavRecord<? extends org.apache.wicket.request.component.IRequestablePage>> crumbs = List.of(
                new RdBreadcrumbNavRecord<>(null, getString("intake.breadcrumb.mijnloket"), MarriageIntakePage.class),
                new RdBreadcrumbNavRecord<>(null, getString("intake.tab.dedag"), DeDagPage.class),
                new RdBreadcrumbNavRecord<>(null, getString("datum.kiezen.breadcrumb"), DatumKiezenPage.class)
        );
        pageBody.add(new RdBreadcrumbNavPanel("breadcrumb", crumbs));

        PageParameters terugParams = new PageParameters()
                .add("dossierId", dossierId.toString());

        pageBody.add(new BookmarkablePageLink<>("terugLink", DeDagPage.class, terugParams));

        pageBody.add(new Label("heading", new ResourceModel("datum.kiezen.heading")));

        pageBody.add(new DatumKiezenForm());
    }

    private class DatumKiezenForm extends Form<LocalDateTime> {

        private final IModel<LocalDateTime> selectedModel = Model.of((LocalDateTime) null);

        private DatumKiezenForm() {
            super("datumKiezenForm");
            setModel(selectedModel);
        }

        @Override
        protected void onInitialize() {
            super.onInitialize();

            IModel<Collection<LocalDateTime>> beschikbareSlots = LoadableDetachableModel.of(
                    () -> marriageIntakeService.findAllBeschikbareSlots(dossierId));

            RdDatePicker datePicker = new RdDatePicker("datePicker", selectedModel) {
                @Override
                public String getInput() {
                    var input = super.getInput();

                    // TODO tijdelijke fix, inputs datepicker zijn local times, outputs met Zulu timezone
                    // converteer naar local, ga uit van Europe/Amsterdam als zone wat we willen.

                    if (input != null && input.endsWith("Z")) {
                        ZonedDateTime zoned = ZonedDateTime.parse(input);
                        ZonedDateTime amsterdam = zoned.withZoneSameInstant(ZoneId.of("Europe/Amsterdam"));
                        LocalDateTime local = amsterdam.toLocalDateTime();
                        input = local.toString();
                    }

                    return input;
                }
            }
                    .withAvailableDateTimes(beschikbareSlots);


            datePicker.setType(LocalDateTime.class);

            add(datePicker,
                    new RdButton("bevestigButton", new ResourceModel("datum.kiezen.bevestig")));
        }

        @Override
        protected void onSubmit() {
            LocalDateTime gekozen = selectedModel.getObject();
            if (gekozen == null) {
                return;
            }
            marriageIntakeService.slaAfspraakOp(dossierId, gekozen.toLocalDate(), gekozen.toLocalTime());
            PageParameters params = new PageParameters();
            params.add("dossierId", dossierId.toString());
            setResponsePage(DeDagPage.class, params);
        }
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(MARRIAGE_INTAKE_CSS);
    }
}

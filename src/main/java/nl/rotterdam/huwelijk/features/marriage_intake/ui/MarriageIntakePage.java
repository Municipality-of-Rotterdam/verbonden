package nl.rotterdam.huwelijk.features.marriage_intake.ui;

import nl.rotterdam.huwelijk.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.CreateDossierDto;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.DossierSamenvattingDto;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.RegistratieType;
import nl.rotterdam.nl_design_system.rotterdam_extensions.wicket.components.rotterdam_icon.RotterdamIconBehavior;
import nl.rotterdam.nl_design_system.rotterdam_extensions.wicket.components.rotterdam_icon.RotterdamIconType;
import nl.rotterdam.nl_design_system.wicket.components.button.RdButton;
import nl.rotterdam.nl_design_system.wicket.components.button.RdButtonAppearance;
import nl.rotterdam.nl_design_system.wicket.components.heading.RdHeading;
import nl.rotterdam.nl_design_system.wicket.components.radio_button.RdRadioButton;
import nl.rotterdam.nl_design_system.wicket.components.radio_group.RdRadioGroup;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

public class MarriageIntakePage extends IntakeBasePage {

    @SpringBean
    private MarriageIntakeService marriageIntakeService;

    private RegistratieType registratieType = RegistratieType.GEREGISTREERD_PARTNERSCHAP;
    private CeremonieSoort ceremonieSoort = CeremonieSoort.EENVOUDIG;

    private RdRadioGroup<RegistratieType> registrationGroup;
    private RdRadioGroup<CeremonieSoort> ceremonyGroup;

    @Override
    protected IntakeStep getActiveStep() {
        return IntakeStep.DE_DAG;
    }

    @Override
    protected IModel<String> getTitleModel() {
        return new ResourceModel("intake.page.title.marriageintake");
    }

    @Override
    protected IModel<DossierSamenvattingDto> getSidebarDossierModel() {
        return () -> new DossierSamenvattingDto(0, registratieType, ceremonieSoort);
    }

    public MarriageIntakePage() {
        this(new PageParameters());
    }

    public MarriageIntakePage(PageParameters parameters) {
        pageBody.add(new RdHeading("heading", getString("intake.heading"), 1));

        Form<Void> form = new Form<>("form");
        pageBody.add(form);

        // Registratie radio group
        registrationGroup = new RdRadioGroup<>(
                "registrationGroup",
                new PropertyModel<>(this, "registratieType"),
                new ResourceModel("intake.registratie.legend"),
                new ResourceModel("intake.registratie.description")
        );
        form.add(registrationGroup);
        RadioGroup<RegistratieType> regRadioGroup = registrationGroup.getRadioGroup();
        registrationGroup.add(new RdRadioButton<>("huwelijk", Model.of(RegistratieType.HUWELIJK), regRadioGroup));
        registrationGroup.add(new RdRadioButton<>("geregistreerdPartnerschap",
                Model.of(RegistratieType.GEREGISTREERD_PARTNERSCHAP), regRadioGroup));

        // Soort radio group
        ceremonyGroup = new RdRadioGroup<>(
                "ceremonyGroup",
                new PropertyModel<>(this, "ceremonieSoort"),
                new ResourceModel("intake.soort.legend"),
                new ResourceModel("intake.soort.description")
        );
        form.add(ceremonyGroup);
        RadioGroup<CeremonieSoort> cerRadioGroup = ceremonyGroup.getRadioGroup();
        ceremonyGroup.add(new RdRadioButton<>("gratis", Model.of(CeremonieSoort.GRATIS), cerRadioGroup));
        ceremonyGroup.add(new RdRadioButton<>("eenvoudig", Model.of(CeremonieSoort.EENVOUDIG), cerRadioGroup));
        ceremonyGroup.add(new RdRadioButton<>("regulier", Model.of(CeremonieSoort.REGULIER), cerRadioGroup));

        // Submit button
        RdButton submitButton = new RdButton("submitButton") {
            @Override
            public void onSubmit() {
                long dossierId = marriageIntakeService.create(
                        new CreateDossierDto(registratieType, ceremonieSoort));
                PageParameters params = new PageParameters();
                params.add("dossierId", dossierId);
                setResponsePage(DeDagPage.class, params);
            }
        };
        submitButton.setAppearance(RdButtonAppearance.PRIMARY_ACTION);
        submitButton.add(new WebMarkupContainer("icon")
                .add(new RotterdamIconBehavior(RotterdamIconType.RING)));

        form.add(submitButton);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        addSidebarUpdateBehavior(registrationGroup);
        addSidebarUpdateBehavior(ceremonyGroup);
    }

    private void addSidebarUpdateBehavior(RdRadioGroup<?> group) {
        group.getRadioGroup().add(new AjaxFormChoiceComponentUpdatingBehavior() {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                target.add(keuzesSidebar);
            }
        });
    }
}


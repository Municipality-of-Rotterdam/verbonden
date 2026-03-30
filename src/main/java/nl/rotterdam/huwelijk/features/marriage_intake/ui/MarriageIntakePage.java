package nl.rotterdam.huwelijk.features.marriage_intake.ui;

import nl.rotterdam.huwelijk.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.CreateDossierDto;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.DossierSamenvattingDto;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.RegistratieType;
import nl.rotterdam.nl_design_system.wicket.components.button.RdButton;
import nl.rotterdam.nl_design_system.wicket.components.heading.RdHeading;
import nl.rotterdam.nl_design_system.wicket.components.radio_button.RdRadioButton;
import nl.rotterdam.nl_design_system.wicket.components.radio_group.RdRadioGroup;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.util.List;
import java.util.UUID;

public class MarriageIntakePage extends IntakeBasePage {

    @SpringBean
    private MarriageIntakeService marriageIntakeService;

    private RegistratieType registratieType = RegistratieType.GEREGISTREERD_PARTNERSCHAP;

    private RdRadioGroup<RegistratieType> registrationGroup;

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
        return () -> new DossierSamenvattingDto(null, registratieType, CeremonieSoort.KLEIN, null, null, false, false, List.of());
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

        // Ceremony submit buttons — each creates the dossier with the chosen ceremony type
        form.add(createCeremonyButton("kleinButton", CeremonieSoort.KLEIN,
                new ResourceModel("intake.soort.klein.title")));

        RdButton middelgrootButton = createCeremonyButton("middelgrootButton", CeremonieSoort.MIDDELGROOT,
                new ResourceModel("intake.soort.middelgroot.title"));
        middelgrootButton.setEnabled(false);
        form.add(middelgrootButton);

        RdButton grootButton = createCeremonyButton("grootButton", CeremonieSoort.GROOT,
                new ResourceModel("intake.soort.groot.title"));
        grootButton.setEnabled(false);
        form.add(grootButton);
    }

    private RdButton createCeremonyButton(String id, CeremonieSoort soort, IModel<String> labelModel) {
        return new RdButton(id, labelModel) {
            @Override
            public void onSubmit() {
                UUID dossierId = marriageIntakeService.create(
                        new CreateDossierDto(registratieType, soort));
                PageParameters params = new PageParameters();
                params.add("dossierId", dossierId.toString());
                setResponsePage(DeDagPage.class, params);
            }
        };
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        registrationGroup.getRadioGroup().add(new AjaxFormChoiceComponentUpdatingBehavior() {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                target.add(keuzesSidebar);
            }
        });
    }
}


package nl.rotterdam.huwelijk.features.marriage_intake.ui;

import nl.rotterdam.huwelijk.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.DossierSamenvattingDto;
import nl.rotterdam.nl_design_system.wicket.components.heading.RdHeading;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

public class DeDagPage extends IntakeBasePage {

    @SpringBean
    private MarriageIntakeService marriageIntakeService;

    private long dossierId;

    @Override
    protected IntakeStep getActiveStep() {
        return IntakeStep.DE_DAG;
    }

    @Override
    protected IModel<String> getTitleModel() {
        return new ResourceModel("intake.page.title.dedag");
    }

    @Override
    protected IModel<DossierSamenvattingDto> getSidebarDossierModel() {
        return Model.of(marriageIntakeService.findById(dossierId));
    }

    public DeDagPage(PageParameters parameters) {
        this.dossierId = parameters.get("dossierId").toLong();

        pageBody.add(new RdHeading("heading", getString("intake.heading"), 1));

        Form<Void> form = new Form<>("form");
        pageBody.add(form);

        form.add(createCeremonyButton("kleinButton", CeremonieSoort.KLEIN));
        form.add(createCeremonyButton("middelgrootButton", CeremonieSoort.MIDDELGROOT));
        form.add(createCeremonyButton("grootButton", CeremonieSoort.GROOT));
    }

    private Button createCeremonyButton(String id, CeremonieSoort soort) {
        return new Button(id) {
            @Override
            public void onSubmit() {
                marriageIntakeService.updateCeremonie(dossierId, soort);
                PageParameters params = new PageParameters();
                params.add("dossierId", dossierId);
                setResponsePage(DeDagPage.class, params);
            }
        };
    }
}


package nl.rotterdam.huwelijk.features.marriage_intake.ui;

import nl.rotterdam.huwelijk.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.DossierSamenvattingDto;
import nl.rotterdam.nl_design_system.wicket.components.heading.RdHeading;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.util.UUID;

public class DeDagPage extends IntakeBasePage {

    @SpringBean
    private MarriageIntakeService marriageIntakeService;

    private UUID dossierId;

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
        return Model.of(marriageIntakeService.findByDossierId(dossierId));
    }

    public DeDagPage(PageParameters parameters) {
        this.dossierId = UUID.fromString(parameters.get("dossierId").toString());
        marriageIntakeService.ensureBsnAccess(dossierId, getCurrentBsn());

        pageBody.add(new RdHeading("heading", getString("intake.heading"), 1));

        pageBody.add(new Link<Void>("datumLink") {
            @Override
            public void onClick() {
                PageParameters params = new PageParameters();
                params.add("dossierId", dossierId.toString());
                setResponsePage(DatumKiezenPage.class, params);
            }
        });

        Link<Void> locatieLink = new Link<Void>("locatieLink") {
            @Override
            public void onClick() {
                // Navigation to location picker — to be implemented in a future iteration
            }
        };
        locatieLink.setEnabled(false);
        pageBody.add(locatieLink);

        Link<Void> babsLink = new Link<Void>("babsLink") {
            @Override
            public void onClick() {
                // Navigation to BABS picker — to be implemented in a future iteration
            }
        };
        babsLink.setEnabled(false);
        pageBody.add(babsLink);

        PageParameters jullieGegevensParams = new PageParameters();
        jullieGegevensParams.add("dossierId", dossierId.toString());
        pageBody.add(new BookmarkablePageLink<>("jullieGegevensLink", JullieGegevensPage.class, jullieGegevensParams));
    }
}


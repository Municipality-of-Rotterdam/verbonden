package nl.rotterdam.verbonden.features.marriage_intake.ui;

import nl.rotterdam.verbonden.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.verbonden.features.marriage_intake.domain.DossierSamenvattingDto;
import nl.rotterdam.nl_design_system.wicket.components.heading.RdHeading;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.util.UUID;

import static nl.rotterdam.verbonden.features.marriage_intake.ui.DossierPageParameterUtil.makeDossierPageParameters;

public class DeDagPage extends IntakeBasePage {

    @SpringBean
    private MarriageIntakeService marriageIntakeService;

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

    public static void respond(UUID dossierId) {
        RequestCycle.get().setResponsePage(
                DeDagPage.class,
                makeDossierPageParameters(dossierId)
        );
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        pageBody.add(new RdHeading("heading", getString("intake.heading"), 1));

        pageBody.add(new Link<Void>("datumLink") {
            @Override
            public void onClick() {
                DatumKiezenPage.respond(dossierId);
            }
        });

        Link<Void> locatieLink = new Link<      >("locatieLink") {
            @Override
            public void onClick() {
                // Navigation to location picker — to be implemented in a future iteration
            }
        };
        locatieLink.setEnabled(false);
        pageBody.add(locatieLink);

        Link<Void> babsLink = new Link<>("babsLink") {
            @Override
            public void onClick() {
                // Navigation to BABS picker — to be implemented in a future iteration
            }
        };
        babsLink.setEnabled(false);
        pageBody.add(babsLink);

        pageBody.add(new BookmarkablePageLink<>("jullieGegevensLink", JullieGegevensPage.class, makeDossierPageParameters(dossierId)));
    }
}


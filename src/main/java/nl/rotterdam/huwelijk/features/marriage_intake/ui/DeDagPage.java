package nl.rotterdam.huwelijk.features.marriage_intake.ui;

import nl.rotterdam.huwelijk.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.DossierSamenvattingDto;
import nl.rotterdam.nl_design_system.rotterdam_extensions.wicket.components.rotterdam_icon.RotterdamIconBehavior;
import nl.rotterdam.nl_design_system.rotterdam_extensions.wicket.components.rotterdam_icon.RotterdamIconType;
import nl.rotterdam.nl_design_system.wicket.components.heading.RdHeading;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.Link;
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

        pageBody.add(new Link<Void>("naarJullieGegevensButton") {
            @Override
            public void onClick() {
                // Navigatie naar stap "Jullie gegevens" volgt in een volgende iteratie
            }
        }.add(new WebMarkupContainer("koppelIcon")
                .add(new RotterdamIconBehavior(RotterdamIconType.RING))));

        // Placeholder links for sub-pages (to be implemented in a future iteration)
        pageBody.add(new Link<Void>("datumLink") {
            @Override
            public void onClick() {
                // Navigatie naar datum-kiezer volgt in een volgende iteratie
            }
        });
        pageBody.add(new Link<Void>("locatieLink") {
            @Override
            public void onClick() {
                // Navigatie naar locatie-kiezer volgt in een volgende iteratie
            }
        });
        pageBody.add(new Link<Void>("bapsLink") {
            @Override
            public void onClick() {
                // Navigatie naar trouwambtenaar-kiezer volgt in een volgende iteratie
            }
        });
    }
}


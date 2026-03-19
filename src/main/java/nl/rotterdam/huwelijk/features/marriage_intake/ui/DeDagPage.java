package nl.rotterdam.huwelijk.features.marriage_intake.ui;

import nl.rotterdam.huwelijk.burger_common.BurgerBasePage;
import nl.rotterdam.huwelijk.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.DossierSamenvattingDto;
import nl.rotterdam.nl_design_system.rotterdam_extensions.wicket.components.rotterdam_icon.RotterdamIconBehavior;
import nl.rotterdam.nl_design_system.rotterdam_extensions.wicket.components.rotterdam_icon.RotterdamIconType;
import nl.rotterdam.nl_design_system.wicket.components.breadcrumb_nav.RdBreadcrumbNavPanel;
import nl.rotterdam.nl_design_system.wicket.components.breadcrumb_nav.RdBreadcrumbNavRecord;
import nl.rotterdam.nl_design_system.wicket.components.heading.RdHeading;
import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.util.List;

public class DeDagPage extends BurgerBasePage {

    @SpringBean
    private MarriageIntakeService marriageIntakeService;

    @Override
    protected IModel<String> getTitleModel() {
        return Model.of("Dé dag - Gemeente Rotterdam");
    }

    public DeDagPage(PageParameters parameters) {
        long dossierId = parameters.get("dossierId").toLong();
        DossierSamenvattingDto dossier = marriageIntakeService.findById(dossierId);

        List<RdBreadcrumbNavRecord<? extends org.apache.wicket.request.component.IRequestablePage>> breadcrumbs = List.of(
                new RdBreadcrumbNavRecord<>(null, "Mijn Loket", MarriageIntakePage.class),
                new RdBreadcrumbNavRecord<>(null, "Mijn dag", DeDagPage.class)
        );
        pageBody.add(new RdBreadcrumbNavPanel("breadcrumb", breadcrumbs));

        pageBody.add(new RdHeading("heading", "Maak jullie dag om nooit te vergeten", 1));

        pageBody.add(new Link<Void>("naarJullieGegevensButton") {
            @Override
            public void onClick() {
                // Navigatie naar stap "Jullie gegevens" volgt in een volgende iteratie
            }
        }.add(new WebMarkupContainer("koppelIcon")
                .add(new RotterdamIconBehavior(RotterdamIconType.RING))));

        pageBody.add(new Label("registratieTypeLabel", dossier.registratieType().getLabel()));
        pageBody.add(new Label("ceremonieSoortLabel", dossier.ceremonieSoort().getLabel()));
        pageBody.add(new Label("ceremoniePrijs", dossier.ceremonieSoort().getPrijs()));

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

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(CssReferenceHeaderItem.forReference(
                new PackageResourceReference(DeDagPage.class, "mijn-dag.css")));
    }
}

package nl.rotterdam.huwelijk.features.marriage_intake.ui;

import nl.rotterdam.huwelijk.burger_common.BurgerBasePage;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.DossierSamenvattingDto;
import nl.rotterdam.nl_design_system.wicket.components.breadcrumb_nav.RdBreadcrumbNavPanel;
import nl.rotterdam.nl_design_system.wicket.components.breadcrumb_nav.RdBreadcrumbNavRecord;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.PackageResourceReference;

import java.util.List;

public abstract class IntakeBasePage extends BurgerBasePage {

    private static final CssReferenceHeaderItem MIJN_DAG_CSS_ITEM =
            CssHeaderItem.forReference(new PackageResourceReference(IntakeBasePage.class, "mijn-dag.css"));

    protected abstract IModel<DossierSamenvattingDto> getSidebarDossierModel();

    @Override
    protected void onInitialize() {
        super.onInitialize();

        List<RdBreadcrumbNavRecord<? extends org.apache.wicket.request.component.IRequestablePage>> breadcrumbs = List.of(
                new RdBreadcrumbNavRecord<>(null, getString("intake.breadcrumb.mijnloket"), MarriageIntakePage.class),
                new RdBreadcrumbNavRecord<>(null, getString("intake.breadcrumb.mijndag"), MarriageIntakePage.class)
        );
        pageBody.add(new RdBreadcrumbNavPanel("breadcrumb", breadcrumbs));
        pageBody.add(new IntakeSidebarPanel("keuzesSidebar", getSidebarDossierModel()));
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(MIJN_DAG_CSS_ITEM);
    }
}

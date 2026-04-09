package nl.rotterdam.huwelijk.features.marriage_intake.ui;

import nl.rotterdam.huwelijk.burger_common.BurgerBasePage;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.DossierSamenvattingDto;
import nl.rotterdam.nl_design_system.wicket.components.breadcrumb_nav.RdBreadcrumbNavPanel;
import nl.rotterdam.nl_design_system.wicket.components.breadcrumb_nav.RdBreadcrumbNavRecord;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.PackageResourceReference;

import java.util.List;

public abstract class IntakeBasePage extends BurgerBasePage {

    private static final CssReferenceHeaderItem MIJN_DAG_CSS_ITEM =
            CssHeaderItem.forReference(new PackageResourceReference(IntakeBasePage.class, "IntakeBasePage.css"));

    protected IntakeSidebarPanel keuzesSidebar;

    protected abstract IModel<DossierSamenvattingDto> getSidebarDossierModel();

    protected abstract IntakeStep getActiveStep();

    @Override
    protected void onInitialize() {
        super.onInitialize();

        List<RdBreadcrumbNavRecord<? extends org.apache.wicket.request.component.IRequestablePage>> breadcrumbs = List.of(
                new RdBreadcrumbNavRecord<>(null, getString("intake.breadcrumb.mijnloket"), MarriageIntakePage.class),
                new RdBreadcrumbNavRecord<>(null, getString("intake.breadcrumb.mijndag"), MarriageIntakePage.class)
        );
        pageBody.add(new RdBreadcrumbNavPanel("breadcrumb", breadcrumbs));

        pageBody.add(createTabButton("tabDedag", IntakeStep.DE_DAG));
        pageBody.add(createTabButton("tabJullieGegevens", IntakeStep.JULLIE_GEGEVENS));
        pageBody.add(createTabButton("tabGetuigen", IntakeStep.DE_GETUIGEN));
        pageBody.add(createTabButton("tabExtras", IntakeStep.EXTRAS));

        keuzesSidebar = new IntakeSidebarPanel("keuzesSidebar", getSidebarDossierModel());
        pageBody.add(keuzesSidebar);
    }

    private WebMarkupContainer createTabButton(String id, IntakeStep step) {
        WebMarkupContainer tab = new WebMarkupContainer(id);
        boolean isActive = getActiveStep() == step;
        tab.add(AttributeModifier.replace("aria-selected", String.valueOf(isActive)));
        if (isActive) {
            tab.add(AttributeModifier.append("class", " rd-tab--active"));
            tab.add(AttributeModifier.replace("aria-current", "step"));
        }
        return tab;
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(MIJN_DAG_CSS_ITEM);
    }
}

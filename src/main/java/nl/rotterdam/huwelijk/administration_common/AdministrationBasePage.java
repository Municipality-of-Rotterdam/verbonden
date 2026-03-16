package nl.rotterdam.huwelijk.administration_common;

import de.agilecoders.wicket.webjars.request.resource.WebjarsCssResourceReference;
import nl.rotterdam.huwelijk.features.baps_administration.ui.BapsAdministrationPage;
import nl.rotterdam.huwelijk.features.location_administration.ui.LocationAdministrationPage;
import nl.rotterdam.nl_design_system.rotterdam_css.wicket.PatchingNldsRotterdamDesignSystemThemeBehavior;
import nl.rotterdam.nl_design_system.wicket.components.side_nav.RdSideNavPanel;
import nl.rotterdam.nl_design_system.wicket.components.side_nav.RdSideNavRecord;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;

import java.util.List;

public abstract class AdministrationBasePage extends WebPage {

    private static final CssReferenceHeaderItem BOOTSTRAP_UTILITIES_HEADER_ITEM =
            CssHeaderItem.forReference(new WebjarsCssResourceReference("bootstrap/current/css/bootstrap-utilities.min.css"));

    public AdministrationBasePage() {
        add(PatchingNldsRotterdamDesignSystemThemeBehavior.INSTANCE);
        add(new RdSideNavPanel("sideNav", List.of(
                new RdSideNavRecord(null, "BAPS Beheer", BapsAdministrationPage.class, null, null, null),
                new RdSideNavRecord(null, "Trouwlocaties Beheer", LocationAdministrationPage.class, null, null, null)
        )));
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(BOOTSTRAP_UTILITIES_HEADER_ITEM);
    }
}

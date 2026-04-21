package nl.rotterdam.huwelijk.administration_common;

import de.agilecoders.wicket.webjars.request.resource.WebjarsCssResourceReference;
import nl.rotterdam.huwelijk.features.babs_administration.ui.BabsAdministrationPage;
import nl.rotterdam.huwelijk.features.dossier_administration.ui.DossierAdministrationPage;
import nl.rotterdam.huwelijk.features.location_administration.ui.LocationAdministrationPage;
import nl.rotterdam.huwelijk.features.marriage_type_administration.ui.MarriageTypeAdministrationPage;
import nl.rotterdam.nl_design_system.rotterdam_css.wicket.PatchingNldsRotterdamDesignSystemThemeBehavior;
import nl.rotterdam.nl_design_system.rotterdam_extensions.wicket.components.rotterdam_icon.RotterdamIconBehavior;
import nl.rotterdam.nl_design_system.rotterdam_extensions.wicket.components.rotterdam_icon.RotterdamIconType;
import nl.rotterdam.nl_design_system.rotterdam_extensions.wicket.components.rotterdam_logo.RotterdamLogoImage;
import nl.rotterdam.nl_design_system.wicket.components.body.RdBodyTransparentContainer;
import nl.rotterdam.nl_design_system.wicket.components.logo.RdLogoBorder;
import nl.rotterdam.nl_design_system.wicket.components.page_body.RdPageBodyBorder;
import nl.rotterdam.nl_design_system.wicket.components.page_footer.RdPageFooterBorder;
import nl.rotterdam.nl_design_system.wicket.components.page_header.RdPageHeaderBorder;
import nl.rotterdam.nl_design_system.wicket.components.page_layout.RdPageLayoutBorder;
import nl.rotterdam.nl_design_system.wicket.components.root.RdRootTransparentContainer;
import nl.rotterdam.nl_design_system.wicket.components.side_nav.RdSideNavPanel;
import nl.rotterdam.nl_design_system.wicket.components.side_nav.RdSideNavRecord;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;

import java.util.List;

public abstract class AdministrationBasePage extends WebPage {

    private static final CssReferenceHeaderItem BOOTSTRAP_GRID_HEADER_ITEM =
            CssHeaderItem.forReference(new WebjarsCssResourceReference("bootstrap/current/css/bootstrap-grid.min.css"));
    private static final CssReferenceHeaderItem BOOTSTRAP_UTILITIES_HEADER_ITEM =
            CssHeaderItem.forReference(new WebjarsCssResourceReference("bootstrap/current/css/bootstrap-utilities.min.css"));

    protected final RdPageBodyBorder pageBody;

    public AdministrationBasePage() {
        add(PatchingNldsRotterdamDesignSystemThemeBehavior.INSTANCE);
        add(new RdRootTransparentContainer("root"));
        add(new RdBodyTransparentContainer("body"));

        RdPageLayoutBorder pageLayout = new RdPageLayoutBorder("pageLayout");
        add(pageLayout);

        RdPageHeaderBorder pageHeader = new RdPageHeaderBorder("pageHeader");
        pageLayout.add(pageHeader);

        RdLogoBorder logo = new RdLogoBorder("logo");
        logo.add(new RotterdamLogoImage("rotterdamLogoImage"));
        pageHeader.add(logo);

        pageHeader.add(new WebMarkupContainer("globeIcon")
                .add(new RotterdamIconBehavior(RotterdamIconType.GLOBE)));
        pageHeader.add(new WebMarkupContainer("userIcon")
                .add(new RotterdamIconBehavior(RotterdamIconType.USER)));
        pageHeader.add(new WebMarkupContainer("logOutIcon")
                .add(new RotterdamIconBehavior(RotterdamIconType.LOG_OUT)));

        pageLayout.add(new RdSideNavPanel("sideNav", List.of(
                new RdSideNavRecord(null, "BABS ", BabsAdministrationPage.class, null, null, null),
                new RdSideNavRecord(null, "Trouwlocaties", LocationAdministrationPage.class, null, null, null),
                new RdSideNavRecord(null, "Huwelijkstypen", MarriageTypeAdministrationPage.class, null, null, null),
                new RdSideNavRecord(null, "Dossiers", DossierAdministrationPage.class, null, null, null)
        )));

        pageBody = new RdPageBodyBorder("pageBody");
        pageLayout.add(pageBody);

        RdPageFooterBorder pageFooter = new RdPageFooterBorder("pageFooter");
        pageFooter.add(new RotterdamLogoImage("footerLogoImage"));
        pageLayout.add(pageFooter);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(BOOTSTRAP_GRID_HEADER_ITEM);
        response.render(BOOTSTRAP_UTILITIES_HEADER_ITEM);
    }
}

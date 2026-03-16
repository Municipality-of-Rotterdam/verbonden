package nl.rotterdam.huwelijk.burger_common;

import nl.rotterdam.nl_design_system.rotterdam_css.wicket.NldsRotterdamDesignSystemThemeBehavior;
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
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;

public abstract class BurgerBasePage extends WebPage {

    protected final RdPageBodyBorder pageBody;

    public BurgerBasePage() {
        RdRootTransparentContainer root = new RdRootTransparentContainer("root");
        root.add(NldsRotterdamDesignSystemThemeBehavior.INSTANCE);
        add(root);

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

        pageBody = new RdPageBodyBorder("pageBody");
        pageLayout.add(pageBody);

        RdPageFooterBorder pageFooter = new RdPageFooterBorder("pageFooter");
        pageFooter.add(new RotterdamLogoImage("footerLogoImage"));
        pageLayout.add(pageFooter);
    }
}

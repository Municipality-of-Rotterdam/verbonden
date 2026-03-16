package nl.rotterdam.huwelijk.burger_common;

import de.agilecoders.wicket.webjars.request.resource.WebjarsCssResourceReference;
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
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public abstract class BurgerBasePage extends WebPage {

    private static final CssReferenceHeaderItem BOOTSTRAP_GRID_HEADER_ITEM =
            CssHeaderItem.forReference(new WebjarsCssResourceReference("bootstrap/current/css/bootstrap-grid.min.css"));
    private static final CssReferenceHeaderItem BOOTSTRAP_UTILITIES_HEADER_ITEM =
            CssHeaderItem.forReference(new WebjarsCssResourceReference("bootstrap/current/css/bootstrap-utilities.min.css"));

    protected final RdPageBodyBorder pageBody;

    protected abstract IModel<String> getTitleModel();

    public BurgerBasePage() {
        add(new Label("pageTitle", getTitleModel()));

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

        WebMarkupContainer userBar = new WebMarkupContainer("userBar") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(isAuthenticated());
            }
        };
        userBar.add(new WebMarkupContainer("userIcon")
                .add(new RotterdamIconBehavior(RotterdamIconType.USER)));
        userBar.add(new Label("userName", this::currentUserName));
        userBar.add(new WebMarkupContainer("logOutIcon")
                .add(new RotterdamIconBehavior(RotterdamIconType.LOG_OUT)));
        pageHeader.add(userBar);

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

    private String currentUserName() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken))
                ? auth.getName() : "";
    }

    private boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
    }
}

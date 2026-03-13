package nl.rotterdam.huwelijk.administration_common;

import de.agilecoders.wicket.webjars.request.resource.WebjarsCssResourceReference;
import nl.rotterdam.nl_design_system.rotterdam_css.wicket.PatchingNldsRotterdamDesignSystemThemeBehavior;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;

public abstract class AdministrationBasePage extends WebPage {

    private static final CssReferenceHeaderItem BOOTSTRAP_UTILITIES_HEADER_ITEM =
            CssHeaderItem.forReference(new WebjarsCssResourceReference("bootstrap/current/css/bootstrap-utilities.min.css"));

    public AdministrationBasePage() {
        add(PatchingNldsRotterdamDesignSystemThemeBehavior.INSTANCE);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(BOOTSTRAP_UTILITIES_HEADER_ITEM);
    }
}

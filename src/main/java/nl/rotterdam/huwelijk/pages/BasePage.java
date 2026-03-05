package nl.rotterdam.huwelijk.pages;

import nl.rotterdam.nl_design_system.rotterdam_css.wicket.NldsRotterdamDesignSystemThemeBehavior;
import org.apache.wicket.markup.html.TransparentWebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;

public abstract class BasePage extends WebPage {

    public BasePage() {
        TransparentWebMarkupContainer html = new TransparentWebMarkupContainer("html");
        html.add(NldsRotterdamDesignSystemThemeBehavior.INSTANCE);
        add(html);
    }
}

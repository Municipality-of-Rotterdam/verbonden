package nl.rotterdam.verbonden.core.burger_common;

import nl.rotterdam.verbonden.core.WicketApplication;
import org.apache.wicket.Page;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;

import java.io.Serializable;
import java.util.List;

import static nl.rotterdam.nl_design_system.wicket.components.breadcrumb_nav.RdBreadcrumbNavListBehavior.BREADCRUMB_NAV_LIST_BEHAVIOR;

/**
 * Breadcrumb for citizen pages that starts with a link to Mijn Loket ({@code verbonden.mijn-loket-url}),
 * followed by links to pages of this application.
 * <p>
 * {@code RdBreadcrumbNavPanel} only supports links to Wicket pages, so this panel renders the same
 * Utrecht breadcrumb markup itself. {@link nl.rotterdam.nl_design_system.wicket.components.breadcrumb_nav.RdBreadcrumbNavListBehavior}
 * adds the breadcrumb stylesheet.
 */
public class MijnLoketBreadcrumbPanel extends Panel {

    public record Kruimel(String label, Class<? extends Page> pagina) implements Serializable {
    }

    private final List<Kruimel> kruimels;

    public MijnLoketBreadcrumbPanel(String id, List<Kruimel> kruimels) {
        super(id);
        this.kruimels = kruimels;
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        WebMarkupContainer lijst = new WebMarkupContainer("lijst");
        lijst.add(BREADCRUMB_NAV_LIST_BEHAVIOR);
        lijst.add(
                new ExternalLink("mijnLoketLink", Model.of(WicketApplication.get().getMijnLoketUrl()),
                        new ResourceModel("intake.breadcrumb.mijnloket")),
                new ListView<>("kruimel", kruimels) {
                    @Override
                    protected void populateItem(ListItem<Kruimel> item) {
                        Kruimel kruimel = item.getModelObject();
                        BookmarkablePageLink<Void> link = new BookmarkablePageLink<>("link", kruimel.pagina()) {
                            @Override
                            protected void onComponentTag(ComponentTag tag) {
                                super.onComponentTag(tag);
                                if (linksTo(getPage())) {
                                    tag.put("role", "link");
                                    tag.put("aria-disabled", "true");
                                    tag.put("aria-current", "page");
                                }
                            }
                        };
                        link.setAutoEnable(true);
                        link.add(new Label("label", kruimel.label()));
                        item.add(link);
                    }
                }
        );
        add(lijst);
    }
}

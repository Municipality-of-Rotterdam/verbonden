package nl.rotterdam.verbonden.core.burger_common;

import de.agilecoders.wicket.webjars.request.resource.WebjarsCssResourceReference;
import nl.rotterdam.verbonden.core.WicketApplication;
import nl.rotterdam.verbonden.core.domain.BurgerServiceNummer;
import nl.rotterdam.verbonden.core.identity.CurrentUserProvider;
import nl.rotterdam.verbonden.core.identity.PersonInfo;
import nl.rotterdam.verbonden.core.identity.PersonLookupService;
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
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.MetaDataHeaderItem;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public abstract class BurgerBasePage extends WebPage {

    private static final HeaderItem BOOTSTRAP_GRID_HEADER_ITEM =
            CssHeaderItem.forReference(new WebjarsCssResourceReference("bootstrap/current/css/bootstrap-grid.min.css"));
    private static final HeaderItem BOOTSTRAP_UTILITIES_HEADER_ITEM =
            CssHeaderItem.forReference(new WebjarsCssResourceReference("bootstrap/current/css/bootstrap-utilities.min.css"));

    private static final Logger log = LoggerFactory.getLogger(BurgerBasePage.class);

    private static final String OFFICIELE_NAAM_SESSION_KEY = BurgerBasePage.class.getName() + ".officieleNaam";

    private static final HeaderItem BURGER_BASE_PAGE_HEADER_ITEM = CssHeaderItem.forReference(new PackageResourceReference(BurgerBasePage.class, "BurgerBasePage.css"));

    protected final RdPageBodyBorder pageBody;

    @SpringBean
    private CurrentUserProvider currentUserProvider;

    @SpringBean
    private PersonLookupService personLookupService;

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
                setVisible(isBurger());
            }
        };
        userBar.add(new WebMarkupContainer("userIcon")
                .add(new RotterdamIconBehavior(RotterdamIconType.USER)));
        userBar.add(new Label("userName", LoadableDetachableModel.of(this::currentUserOfficieleNaam)));
        userBar.add(new ExternalLink("logOutLink", WicketApplication.get().getBurgerLogoutUrl())
                .add(new WebMarkupContainer("logOutIcon")
                        .add(new RotterdamIconBehavior(RotterdamIconType.LOG_OUT))));
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
        response.render(BURGER_BASE_PAGE_HEADER_ITEM);
        // De Stencil-iconen (rods-icon-*) voegen zelf <style>-blokken toe; via deze meta-tag geven ze
        // die de CSP-nonce mee, anders blokkeert de style-src-directive ze.
        response.render(MetaDataHeaderItem.forMetaTag("csp-nonce",
                WebApplication.get().getCspSettings().getNonce(getRequestCycle())));
    }

    /**
     * De officiële naam (voorletters + achternaam) wordt één keer per sessie opgezocht (niet
     * bij elke paginaweergave een BRP-bevraging) en gekoppeld aan het BSN, zodat een andere
     * burger in dezelfde sessie nooit de naam van de vorige ziet. Faalt het opzoeken, dan blijft de naam leeg, zodat
     * pagina's (en de foutpagina) blijven werken. Een ongeldig BSN blijft wel een harde fout.
     */
    private String currentUserOfficieleNaam() {
        if (!isBurger()) {
            return "";
        }
        BurgerServiceNummer bsn = getCurrentBsn();
        if (getSession().getAttribute(OFFICIELE_NAAM_SESSION_KEY) instanceof SessionOfficieleNaam(
                String cachedBsn, String cachedOfficieleNaam
        )
                && cachedBsn.equals(bsn.getValue())) {
            return cachedOfficieleNaam;
        }
        try {
            String officieleNaam = personLookupService.findByBsn(bsn)
                    .map(PersonInfo::officieleNaam)
                    .orElse("");
            getSession().setAttribute(OFFICIELE_NAAM_SESSION_KEY, new SessionOfficieleNaam(bsn.getValue(), officieleNaam));
            return officieleNaam;
        } catch (RuntimeException e) {
            log.warn("Naam voor de header kon niet worden opgehaald", e);
            return "";
        }
    }

    private record SessionOfficieleNaam(String bsn, String officieleNaam) implements Serializable {
    }

    /**
     * Alleen een burger heeft een BSN. Ook een medewerker of een anoniem gemaakte bezoeker kan een
     * (fout)pagina met deze header zien; voor hen blijft de gebruikersbalk weg.
     */
    private boolean isBurger() {
        return currentUserProvider.getCurrentUser().hasRole("BURGER");
    }

    protected BurgerServiceNummer getCurrentBsn() {
        return new BurgerServiceNummer(currentUserProvider.getCurrentUser().getUserId());
    }
}

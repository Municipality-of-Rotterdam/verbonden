package nl.rotterdam.verbonden.core.administration_common;

import de.agilecoders.wicket.webjars.request.resource.WebjarsCssResourceReference;
import nl.rotterdam.verbonden.core.WicketApplication;
import nl.rotterdam.verbonden.core.features.babs_administration.ui.BabsAdministrationPage;
import nl.rotterdam.verbonden.core.features.dossier_administration.ui.DossierAdministrationPage;
import nl.rotterdam.verbonden.core.features.location_administration.ui.LocationAdministrationPage;
import nl.rotterdam.verbonden.core.features.marriage_type_administration.ui.MarriageTypeAdministrationPage;
import nl.rotterdam.verbonden.core.identity.Roles;
import nl.rotterdam.verbonden.core.features.trouwboekje_administration.ui.TrouwboekjeAdministrationPage;
import nl.rotterdam.verbonden.core.features.trouwlijst_administration.ui.TrouwlijstAdministrationPage;
import nl.rotterdam.nl_design_system.rotterdam_css.wicket.PatchingNldsRotterdamDesignSystemThemeBehavior;
import nl.rotterdam.nl_design_system.rotterdam_extensions.wicket.components.rotterdam_icon.RotterdamIconBehavior;
import nl.rotterdam.nl_design_system.rotterdam_extensions.wicket.components.rotterdam_icon.RotterdamIconType;
import nl.rotterdam.nl_design_system.wicket.components.body.RdBodyTransparentContainer;
import nl.rotterdam.nl_design_system.wicket.components.page_body.RdPageBodyBorder;
import nl.rotterdam.nl_design_system.wicket.components.root.RdRootTransparentContainer;
import org.apache.wicket.Application;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.MetaDataHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.LambdaModel;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.resource.PackageResourceReference;

import java.io.Serializable;
import java.util.List;

/**
 * Basis voor alle beheerpagina's. Standaard mogen alle medewerkersrollen behalve {@code BABS} een beheerpagina
 * openen; een pagina die ook (of juist alleen) voor andere rollen is, overschrijft dit met een eigen
 * {@link AuthorizeInstantiation}. De rolnamen staan in {@link Roles}.
 */
@AuthorizeInstantiation({Roles.BEHEER, Roles.SUPERUSER, Roles.KEYUSER, Roles.WERKVERDELER, Roles.PLANNER,
        Roles.MEDEWERKER})
public abstract class AdministrationBasePage extends WebPage {

    private static final CssReferenceHeaderItem BOOTSTRAP_GRID_HEADER_ITEM =
            CssHeaderItem.forReference(new WebjarsCssResourceReference("bootstrap/current/css/bootstrap-grid.min.css"));
    private static final CssReferenceHeaderItem BOOTSTRAP_UTILITIES_HEADER_ITEM =
            CssHeaderItem.forReference(new WebjarsCssResourceReference("bootstrap/current/css/bootstrap-utilities.min.css"));
    private static final CssReferenceHeaderItem ADMINISTRATION_BASE_PAGE_HEADER_ITEM =
            CssHeaderItem.forReference(new PackageResourceReference(AdministrationBasePage.class, "AdministrationBasePage.css"));

    private static final String SIDEBAR_UITGEKLAPT_SESSION_KEY = "administrationSidebarUitgeklapt";

    private static final List<NavigationItem> NAVIGATION_ITEMS = List.of(
            new NavigationItem("BABS", RotterdamIconType.USER, BabsAdministrationPage.class),
            new NavigationItem("Trouwlocaties", RotterdamIconType.LOCATION, LocationAdministrationPage.class),
            new NavigationItem("Huwelijkstypen", RotterdamIconType.RING, MarriageTypeAdministrationPage.class),
            new NavigationItem("Dossiers", RotterdamIconType.DOCUMENT, DossierAdministrationPage.class),
            new NavigationItem("Trouwboekjes", RotterdamIconType.FILE, TrouwboekjeAdministrationPage.class),
            new NavigationItem("Trouwlijst", RotterdamIconType.CALENDAR, TrouwlijstAdministrationPage.class)
    );

    protected final RdPageBodyBorder pageBody;

    public AdministrationBasePage() {
        add(PatchingNldsRotterdamDesignSystemThemeBehavior.INSTANCE);
        add(new RdRootTransparentContainer("root"));
        add(new RdBodyTransparentContainer("body"));

        add(new SidebarToggle("sidebarToggle"));
        add(new ListView<>("navigationItems", NAVIGATION_ITEMS.stream()
                .filter(item -> magOpenen(item.page()))
                .toList()) {
            @Override
            protected void populateItem(ListItem<NavigationItem> item) {
                NavigationItem navigationItem = item.getModelObject();
                BookmarkablePageLink<Void> pageLink =
                        new BookmarkablePageLink<>("pageLink", navigationItem.page());
                pageLink.add(AttributeModifier.replace("title", navigationItem.label()));
                // Een vervolgpagina (bijv. wijzigen) hoort bij het menu-item van het overzicht in dezelfde package
                Class<? extends Page> huidigePagina = getPage().getClass();
                if (huidigePagina == navigationItem.page()) {
                    pageLink.add(AttributeModifier.replace("aria-current", "page"));
                } else if (huidigePagina.getPackageName().equals(navigationItem.page().getPackageName())) {
                    pageLink.add(AttributeModifier.replace("aria-current", "true"));
                }
                pageLink.add(new WebMarkupContainer("navIcon")
                        .add(new RotterdamIconBehavior(navigationItem.icon())));
                pageLink.add(new Label("navLabel", navigationItem.label()));
                item.add(pageLink);
            }
        });
        add(new WebMarkupContainer("userIcon")
                .add(new RotterdamIconBehavior(RotterdamIconType.USER)));
        add(new ExternalLink("logOutLink", WicketApplication.get().getBeheerLogoutUrl())
                .add(new WebMarkupContainer("logOutIcon")
                        .add(new RotterdamIconBehavior(RotterdamIconType.LOG_OUT))));

        pageBody = new RdPageBodyBorder("pageBody");
        add(pageBody);
    }

    /**
     * Of de ingelogde gebruiker deze pagina mag openen (volgens zijn {@link AuthorizeInstantiation}). Gebruik dit om
     * links en acties te verbergen die alleen zijn voor wie de bijbehorende pagina mag openen.
     */
    protected static boolean magOpenen(Class<? extends Page> pagina) {
        return Application.get().getSecuritySettings().getAuthorizationStrategy().isInstantiationAuthorized(pagina);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(BOOTSTRAP_GRID_HEADER_ITEM);
        response.render(BOOTSTRAP_UTILITIES_HEADER_ITEM);
        response.render(ADMINISTRATION_BASE_PAGE_HEADER_ITEM);
        // De Stencil-iconen (rods-icon-*) voegen zelf <style>-blokken toe; via deze meta-tag geven ze
        // die de CSP-nonce mee, anders blokkeert de style-src-directive ze.
        response.render(MetaDataHeaderItem.forMetaTag("csp-nonce",
                WebApplication.get().getCspSettings().getNonce(getRequestCycle())));
    }

    /** Onthoudt in de sessie of de gebruiker de zijbalk heeft uitgeklapt, zodat die zo blijft op elke pagina. */
    private static class SidebarToggle extends CheckBox {

        SidebarToggle(String id) {
            super(id, LambdaModel.of(
                    () -> Boolean.TRUE.equals(Session.get().getAttribute(SIDEBAR_UITGEKLAPT_SESSION_KEY)),
                    uitgeklapt -> {
                        Session.get().bind();
                        Session.get().setAttribute(SIDEBAR_UITGEKLAPT_SESSION_KEY, uitgeklapt);
                    }));
            // De <label for> in de markup verwijst naar dit id
            setMarkupId("administration-sidebar-toggle");
        }

        @Override
        protected void onInitialize() {
            super.onInitialize();
            add(new AjaxFormComponentUpdatingBehavior("change") {
                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    // Het model heeft de nieuwe stand al in de sessie gezet
                }
            });
        }
    }

    private record NavigationItem(String label, RotterdamIconType icon, Class<? extends Page> page)
            implements Serializable {
    }
}

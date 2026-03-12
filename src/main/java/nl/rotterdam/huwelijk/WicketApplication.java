package nl.rotterdam.huwelijk;

import de.agilecoders.wicket.webjars.WicketWebjars;
import nl.rotterdam.huwelijk.features.baps_administration.ui.BapsToevoegenPage;
import nl.rotterdam.huwelijk.features.baps_administration.ui.BapsWijzigenPage;
import nl.rotterdam.huwelijk.features.baps_administration.ui.BeheerPage;
import nl.rotterdam.huwelijk.features.marriage_intake.ui.HomePage;
import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.FetchMetadataResourceIsolationPolicy;
import org.apache.wicket.protocol.http.ResourceIsolationRequestCycleListener;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;

public class WicketApplication extends WebApplication {

    @Override
    public Class<? extends Page> getHomePage() {
        return HomePage.class;
    }

    @Override
    public void init() {
        super.init();
        WicketWebjars.install(this);
        getComponentInstantiationListeners().add(new SpringComponentInjector(this));

        getMarkupSettings().setStripWicketTags(true);

        // Enable Wicket's built-in CSRF protection via Fetch Metadata headers.
        // Spring Security's CSRF filter is disabled in SecurityConfig to avoid conflicts
        // with Wicket's own form submission mechanism (see SecurityConfig for details).
        getRequestCycleListeners().add(
                new ResourceIsolationRequestCycleListener(
                        new FetchMetadataResourceIsolationPolicy()));

        mountPage("/beheer", BeheerPage.class);
        mountPage("/beheer/baps/nieuw", BapsToevoegenPage.class);
        mountPage("/beheer/baps/${id}", BapsWijzigenPage.class);
    }
}

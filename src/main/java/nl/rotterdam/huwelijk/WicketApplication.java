package nl.rotterdam.huwelijk;

import nl.rotterdam.huwelijk.pages.HomePage;
import nl.rotterdam.huwelijk.pages.beheer.BapsFormPage;
import nl.rotterdam.huwelijk.pages.beheer.BeheerPage;
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
        getComponentInstantiationListeners().add(new SpringComponentInjector(this));

        // Enable Wicket's built-in CSRF protection via Fetch Metadata headers.
        // Spring Security's CSRF filter is disabled in SecurityConfig to avoid conflicts
        // with Wicket's own form submission mechanism (see SecurityConfig for details).
        getRequestCycleListeners().add(
                new ResourceIsolationRequestCycleListener(
                        new FetchMetadataResourceIsolationPolicy()));

        mountPage("/beheer", BeheerPage.class);
        mountPage("/beheer/baps/nieuw", BapsFormPage.class);
        mountPage("/beheer/baps/${id}", BapsFormPage.class);
    }
}

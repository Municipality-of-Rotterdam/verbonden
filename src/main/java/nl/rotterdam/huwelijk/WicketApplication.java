package nl.rotterdam.huwelijk;

import de.agilecoders.wicket.webjars.WicketWebjars;
import nl.rotterdam.huwelijk.features.baps_administration.domain.PersonFullName;
import nl.rotterdam.huwelijk.features.baps_administration.ui.BapsCreatePage;
import nl.rotterdam.huwelijk.features.baps_administration.ui.BapsUpdatePage;
import nl.rotterdam.huwelijk.features.baps_administration.ui.BapsAdministrationPage;
import nl.rotterdam.huwelijk.features.baps_administration.ui.PersonFullNameWicketConverter;
import nl.rotterdam.huwelijk.features.location_administration.ui.BeschikbaarheidCreatePage;
import nl.rotterdam.huwelijk.features.location_administration.ui.BeschikbaarheidUpdatePage;
import nl.rotterdam.huwelijk.features.location_administration.ui.LocationAdministrationPage;
import nl.rotterdam.huwelijk.features.location_administration.ui.LocationCreatePage;
import nl.rotterdam.huwelijk.features.location_administration.ui.LocationUpdatePage;
import nl.rotterdam.huwelijk.features.marriage_intake.ui.DeDagPage;
import nl.rotterdam.huwelijk.features.marriage_intake.ui.MarriageIntakePage;
import org.apache.wicket.ConverterLocator;
import org.apache.wicket.IConverterLocator;
import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.FetchMetadataResourceIsolationPolicy;
import org.apache.wicket.protocol.http.ResourceIsolationRequestCycleListener;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;

import java.time.LocalDate;
import java.time.LocalTime;

public class WicketApplication extends WebApplication {

    @Override
    protected IConverterLocator newConverterLocator() {
        ConverterLocator locator = (ConverterLocator) super.newConverterLocator();
        locator.set(LocalDate.class, new LocalDateWicketConverter());
        locator.set(LocalTime.class, new LocalTimeWicketConverter());
        locator.set(PersonFullName.class, new PersonFullNameWicketConverter());
        return locator;
    }

    @Override
    public Class<? extends Page> getHomePage() {
        return MarriageIntakePage.class;
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

        mountPage("/beheer", BapsAdministrationPage.class);
        mountPage("/beheer/baps/nieuw", BapsCreatePage.class);
        mountPage("/beheer/baps/${id}", BapsUpdatePage.class);

        mountPage("/beheer/locaties", LocationAdministrationPage.class);
        mountPage("/beheer/locaties/nieuw", LocationCreatePage.class);
        mountPage("/beheer/locaties/${id}", LocationUpdatePage.class);
        mountPage("/beheer/locaties/${locatieId}/beschikbaarheden/nieuw", BeschikbaarheidCreatePage.class);
        mountPage("/beheer/locaties/${locatieId}/beschikbaarheden/${id}", BeschikbaarheidUpdatePage.class);

        mountPage("/mijn-dag/${dossierId}", DeDagPage.class);
    }
}

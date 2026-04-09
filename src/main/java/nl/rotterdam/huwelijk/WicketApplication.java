package nl.rotterdam.huwelijk;

import de.agilecoders.wicket.webjars.WicketWebjars;
import nl.rotterdam.huwelijk.burger_common.BurgerErrorPage;
import nl.rotterdam.huwelijk.features.babs_administration.domain.PersonFullName;
import nl.rotterdam.huwelijk.features.babs_administration.ui.BabsCreatePage;
import nl.rotterdam.huwelijk.features.babs_administration.ui.BabsUpdatePage;
import nl.rotterdam.huwelijk.features.babs_administration.ui.BabsAdministrationPage;
import nl.rotterdam.huwelijk.features.babs_administration.ui.PersonFullNameWicketConverter;
import nl.rotterdam.huwelijk.features.location_administration.ui.BeschikbaarheidCreatePage;
import nl.rotterdam.huwelijk.features.location_administration.ui.BeschikbaarheidUpdatePage;
import nl.rotterdam.huwelijk.features.location_administration.ui.LocationAdministrationPage;
import nl.rotterdam.huwelijk.features.location_administration.ui.LocationCreatePage;
import nl.rotterdam.huwelijk.features.location_administration.ui.LocationUpdatePage;
import nl.rotterdam.huwelijk.features.marriage_intake.ui.DatumKiezenPage;
import nl.rotterdam.huwelijk.features.marriage_intake.ui.DeDagPage;
import nl.rotterdam.huwelijk.features.location_administration.ui.NietBeschikbareDagCreatePage;
import nl.rotterdam.huwelijk.features.location_administration.ui.NietBeschikbareDagImportPage;
import nl.rotterdam.huwelijk.features.location_administration.ui.NietBeschikbareDagUpdatePage;
import nl.rotterdam.huwelijk.features.marriage_intake.ui.MarriageIntakePage;
import nl.rotterdam.huwelijk.features.marriage_type_administration.ui.MarriageTypeAdministrationPage;
import nl.rotterdam.huwelijk.features.marriage_type_administration.ui.MarriageTypeCreatePage;
import nl.rotterdam.huwelijk.features.marriage_type_administration.ui.MarriageTypeUpdatePage;
import nl.rotterdam.huwelijk.features.mock_digid.ui.MockDigiDLoginPage;
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

        getApplicationSettings().setInternalErrorPage(BurgerErrorPage.class);
        getApplicationSettings().setPageExpiredErrorPage(BurgerErrorPage.class);
        getApplicationSettings().setAccessDeniedPage(BurgerErrorPage.class);

        // Enable Wicket's built-in CSRF protection via Fetch Metadata headers.
        // Spring Security's CSRF filter is disabled in SecurityConfig to avoid conflicts
        // with Wicket's own form submission mechanism (see SecurityConfig for details).
        getRequestCycleListeners().add(
                new ResourceIsolationRequestCycleListener(
                        new FetchMetadataResourceIsolationPolicy()));

        mountPage("/inloggen", MockDigiDLoginPage.class);
        mountPage("/error", BurgerErrorPage.class);
        mountPage("/beheer/huwelijkstypen", MarriageTypeAdministrationPage.class);
        mountPage("/beheer/huwelijkstypen/nieuw", MarriageTypeCreatePage.class);
        mountPage("/beheer/huwelijkstypen/${id}", MarriageTypeUpdatePage.class);
        mountPage("/beheer", BabsAdministrationPage.class);
        mountPage("/beheer/babs/nieuw", BabsCreatePage.class);
        mountPage("/beheer/babs/${id}", BabsUpdatePage.class);

        mountPage("/beheer/locaties", LocationAdministrationPage.class);
        mountPage("/beheer/locaties/nieuw", LocationCreatePage.class);
        mountPage("/beheer/locaties/${id}", LocationUpdatePage.class);
        mountPage("/beheer/locaties/${locatieId}/beschikbaarheden/nieuw", BeschikbaarheidCreatePage.class);
        mountPage("/beheer/locaties/${locatieId}/beschikbaarheden/${id}", BeschikbaarheidUpdatePage.class);

        mountPage("/mijn-dag/${dossierId}", DeDagPage.class);
        mountPage("/mijn-dag/${dossierId}/datum-kiezen", DatumKiezenPage.class);
        mountPage("/beheer/locaties/${locatieId}/niet-beschikbare-dagen/nieuw", NietBeschikbareDagCreatePage.class);
        mountPage("/beheer/locaties/${locatieId}/niet-beschikbare-dagen/${id}", NietBeschikbareDagUpdatePage.class);
        mountPage("/beheer/locaties/${locatieId}/niet-beschikbare-dagen/importeren", NietBeschikbareDagImportPage.class);
    }
}

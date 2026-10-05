package nl.rotterdam.verbonden.core;

import nl.rotterdam.verbonden.core.burger_common.BurgerAccessDeniedPage;
import nl.rotterdam.verbonden.core.burger_common.BurgerErrorPage;
import nl.rotterdam.verbonden.core.burger_common.BurgerInternalErrorPage;
import nl.rotterdam.verbonden.core.burger_common.BurgerPageExpiredPage;
import nl.rotterdam.verbonden.core.domain.PersonFullName;
import nl.rotterdam.verbonden.core.error_common.VerbondenExceptionMapper;
import nl.rotterdam.verbonden.core.features.trouwboekje_administration.ui.TrouwboekjeAdministrationPage;
import nl.rotterdam.verbonden.core.features.trouwboekje_administration.ui.TrouwboekjeCreatePage;
import nl.rotterdam.verbonden.core.features.trouwboekje_administration.ui.TrouwboekjeUpdatePage;
import nl.rotterdam.verbonden.core.features.marriage_intake.ui.ExtrasPage;
import nl.rotterdam.verbonden.core.features.babs_administration.ui.BabsCreatePage;
import nl.rotterdam.verbonden.core.features.babs_administration.ui.BabsUpdatePage;
import nl.rotterdam.verbonden.core.features.babs_administration.ui.BabsAdministrationPage;
import nl.rotterdam.verbonden.core.features.dossier_administration.ui.DossierAdministrationPage;
import nl.rotterdam.verbonden.core.features.babs_administration.ui.PersonFullNameWicketConverter;
import nl.rotterdam.verbonden.core.domain.Emailadres;
import nl.rotterdam.verbonden.core.domain.Telefoonnummer;
import nl.rotterdam.verbonden.core.features.marriage_intake.ui.EmailadresWicketConverter;
import nl.rotterdam.verbonden.core.features.marriage_intake.ui.TelefoonnummerWicketConverter;
import nl.rotterdam.verbonden.core.features.location_administration.ui.BeschikbaarheidCreatePage;
import nl.rotterdam.verbonden.core.features.location_administration.ui.BeschikbaarheidUpdatePage;
import nl.rotterdam.verbonden.core.features.location_administration.ui.LocationAdministrationPage;
import nl.rotterdam.verbonden.core.features.location_administration.ui.LocationCreatePage;
import nl.rotterdam.verbonden.core.features.location_administration.ui.LocationUpdatePage;
import nl.rotterdam.verbonden.core.features.marriage_intake.ui.DatumKiezenPage;
import nl.rotterdam.verbonden.core.features.marriage_intake.ui.DeDagPage;
import nl.rotterdam.verbonden.core.features.marriage_intake.ui.DeGetuigenPage;
import nl.rotterdam.verbonden.core.features.marriage_intake.ui.DossierUitnodigingPage;
import nl.rotterdam.verbonden.core.features.marriage_intake.ui.JullieGegevensPage;
import nl.rotterdam.verbonden.core.features.location_administration.ui.NietBeschikbareDagCreatePage;
import nl.rotterdam.verbonden.core.features.location_administration.ui.NietBeschikbareDagImportPage;
import nl.rotterdam.verbonden.core.features.location_administration.ui.NietBeschikbareDagUpdatePage;
import nl.rotterdam.verbonden.core.features.marriage_intake.ui.MarriageIntakePage;
import nl.rotterdam.verbonden.core.features.marriage_type_administration.ui.MarriageTypeAdministrationPage;
import nl.rotterdam.verbonden.core.features.marriage_type_administration.ui.MarriageTypeCreatePage;
import nl.rotterdam.verbonden.core.features.marriage_type_administration.ui.MarriageTypeUpdatePage;
import nl.rotterdam.verbonden.core.identity.BurgerLoginPageMount;
import com.giffing.wicket.spring.boot.starter.app.WicketBootStandardWebApplication;
import org.apache.wicket.ConverterLocator;
import org.apache.wicket.IConverterLocator;
import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.SecurePackageResourceGuard;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.apache.wicket.csp.CSPDirective.*;
import static org.apache.wicket.csp.CSPDirectiveSrcValue.NONE;
import static org.apache.wicket.csp.CSPDirectiveSrcValue.SELF;

@Component
public class WicketApplication extends WicketBootStandardWebApplication {

    private final List<BurgerLoginPageMount> burgerLoginPageMounts;
    private final String beheerLogoutUrl;
    private final String burgerLogoutUrl;
    private final ObjectProvider<WicketSessionFactory> sessionFactoryProvider;

    public WicketApplication(List<BurgerLoginPageMount> burgerLoginPageMounts,
                             @Value("${verbonden.logout.beheer-url}") String beheerLogoutUrl,
                             @Value("${verbonden.logout.burger-url}") String burgerLogoutUrl,
                             ObjectProvider<WicketSessionFactory> sessionFactoryProvider) {
        this.burgerLoginPageMounts = burgerLoginPageMounts;
        this.beheerLogoutUrl = beheerLogoutUrl;
        this.burgerLogoutUrl = burgerLogoutUrl;
        this.sessionFactoryProvider = sessionFactoryProvider;
    }

    public static WicketApplication get() {
        return (WicketApplication) WebApplication.get();
    }

    /** URL van de Uitloggen-link op beheerpagina's ({@code verbonden.logout.beheer-url}). */
    public String getBeheerLogoutUrl() {
        return beheerLogoutUrl;
    }

    /** URL van de Uitloggen-link op burgerpagina's ({@code verbonden.logout.burger-url}). */
    public String getBurgerLogoutUrl() {
        return burgerLogoutUrl;
    }

    @Override
    protected IConverterLocator newConverterLocator() {
        ConverterLocator locator = (ConverterLocator) super.newConverterLocator();
        locator.set(LocalDate.class, new LocalDateWicketConverter());
        locator.set(LocalTime.class, new LocalTimeWicketConverter());
        locator.set(PersonFullName.class, new PersonFullNameWicketConverter());
        locator.set(Telefoonnummer.class, new TelefoonnummerWicketConverter());
        locator.set(Emailadres.class, new EmailadresWicketConverter());
        return locator;
    }

    /** Delegeert naar een {@link WicketSessionFactory}-bean als een adapter-module die aanbiedt. */
    @Override
    public Session newSession(Request request, Response response) {
        WicketSessionFactory sessionFactory = sessionFactoryProvider.getIfAvailable();
        return sessionFactory != null
                ? sessionFactory.newSession(request, response)
                : super.newSession(request, response);
    }

    @Override
    public Class<? extends Page> getHomePage() {
        return MarriageIntakePage.class;
    }

    @Override
    public void init() {
        super.init();
        getMarkupSettings().setStripWicketTags(true);

        getApplicationSettings().setInternalErrorPage(BurgerInternalErrorPage.class);
        getApplicationSettings().setPageExpiredErrorPage(BurgerPageExpiredPage.class);
        getApplicationSettings().setAccessDeniedPage(BurgerAccessDeniedPage.class);
        // Kiest bij een /beheer-request de beheervariant van bovenstaande foutpagina's
        setExceptionMapperProvider(VerbondenExceptionMapper::new);

        // Wicket staat webp standaard niet toe als package resource (avif wel); nodig voor o.a. de foto's
        // op de keuzekaarten van DeDagPage, die naast marriage-intake.css staan.
        if (getResourceSettings().getPackageResourceGuard() instanceof SecurePackageResourceGuard guard) {
            guard.addPattern("+*.webp");
        }

        // Extend Wicket's default blocking CSP to allow data: images (needed for QR codes)
        // and style-src 'self'. Wicket's internalInit() already calls reportBack(), so
        // we must NOT call it here again — doing so causes an IllegalArgumentException
        // ("report-uri directive can only contain one URI") in tests and at runtime.
        getCspSettings().blocking()
                .add(STYLE_SRC, SELF)
                .add(IMG_SRC, "data:")
                .add(FRAME_ANCESTORS, NONE);

        for (BurgerLoginPageMount mount : burgerLoginPageMounts) {
            mountPage(mount.getPath(), mount.getPageClass());
        }
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

        mountPage("/huwelijk/${dossierId}", MarriageIntakePage.class);
        mountPage("/huwelijk/${dossierId}/uitnodiging", DossierUitnodigingPage.class);
        mountPage("/mijn-dag/${dossierId}", DeDagPage.class);
        mountPage("/mijn-dag/${dossierId}/jullie-gegevens", JullieGegevensPage.class);
        mountPage("/mijn-dag/${dossierId}/de-getuigen", DeGetuigenPage.class);
        mountPage("/mijn-dag/${dossierId}/datum-kiezen", DatumKiezenPage.class);
        mountPage("/beheer/dossiers", DossierAdministrationPage.class);
        mountPage("/beheer/extras", TrouwboekjeAdministrationPage.class);
        mountPage("/beheer/extras/nieuw", TrouwboekjeCreatePage.class);
        mountPage("/beheer/extras/${id}", TrouwboekjeUpdatePage.class);
        mountPage("/mijn-dag/${dossierId}/extras", ExtrasPage.class);
        mountPage("/beheer/locaties/${locatieId}/niet-beschikbare-dagen/nieuw", NietBeschikbareDagCreatePage.class);
        mountPage("/beheer/locaties/${locatieId}/niet-beschikbare-dagen/${id}", NietBeschikbareDagUpdatePage.class);
        mountPage("/beheer/locaties/${locatieId}/niet-beschikbare-dagen/importeren", NietBeschikbareDagImportPage.class);
    }
}

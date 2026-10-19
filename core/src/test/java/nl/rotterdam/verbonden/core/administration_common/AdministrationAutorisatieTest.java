package nl.rotterdam.verbonden.core.administration_common;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import nl.rotterdam.verbonden.core.features.babs_administration.ui.BabsAdministrationPage;
import nl.rotterdam.verbonden.core.features.babs_administration.ui.BabsCreatePage;
import nl.rotterdam.verbonden.core.features.dossier_administration.ui.DossierAdministrationPage;
import nl.rotterdam.verbonden.core.identity.Roles;
import nl.rotterdam.verbonden.core.integration_test.BaseWicketTest;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Bewaakt welke rol welke beheerpagina mag openen ({@code @AuthorizeInstantiation}). Alle beheerpagina's worden
 * automatisch gevonden, zodat ook een nieuwe pagina tegen elke rol uit {@link Roles} getest wordt.
 */
@Transactional
class AdministrationAutorisatieTest extends BaseWicketTest {

    private static final List<Class<?>> BEHEER_PAGINAS = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("nl.rotterdam.verbonden.core")
            .stream()
            .filter(c -> c.isAssignableTo(AdministrationBasePage.class))
            .filter(c -> !c.isAssignableTo(AdministrationErrorPage.class))
            .filter(c -> !c.getModifiers().contains(JavaModifier.ABSTRACT))
            .map(JavaClass::reflect)
            .toList();

    @Autowired
    private TestRestTemplate restTemplate;

    @AfterEach
    void logUit() {
        SecurityContextHolder.clearContext();
    }

    static Stream<Arguments> paginaEnRol() {
        return BEHEER_PAGINAS.stream()
                .flatMap(pagina -> Roles.ALLE.stream().map(rol -> Arguments.of(pagina, rol)));
    }

    /** Alle rollen behalve BABS. */
    static Stream<String> medewerkersrollen() {
        return Roles.ALLE.stream().filter(rol -> !rol.equals(Roles.BABS));
    }

    static Stream<Class<?>> beheerPaginas() {
        return BEHEER_PAGINAS.stream();
    }

    @Test
    void vindtDeBeheerpaginas() {
        assertThat(BEHEER_PAGINAS).contains(BabsAdministrationPage.class, DossierAdministrationPage.class);
    }

    /** De startpagina is voor iedere rol; alle andere beheerpagina's voor iedere rol behalve BABS. */
    @ParameterizedTest(name = "{0} met rol {1}")
    @MethodSource("paginaEnRol")
    void elkeRolMagBijDeVoorHaarBedoeldePaginas(Class<?> pagina, String rol) {
        alsGebruikerMetRol(rol);

        boolean verwacht = pagina == BabsAdministrationPage.class || !rol.equals(Roles.BABS);
        assertThat(magOpenen(pagina)).isEqualTo(verwacht);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("beheerPaginas")
    void zonderMedewerkersrolGeenToegang(Class<?> pagina) {
        alsGebruikerMetRol("BURGER");

        assertThat(magOpenen(pagina)).isFalse();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("beheerPaginas")
    void zonderRolGeenToegang(Class<?> pagina) {
        assertThat(magOpenen(pagina)).isFalse();
    }

    /** Anders kan de toegang-geweigerd-pagina zelf niet getoond worden. */
    @Test
    void foutpaginasZijnVoorIedereen() {
        alsGebruikerMetRol("BURGER");

        assertThat(magOpenen(AdministrationErrorPage.class)).isTrue();
        assertThat(magOpenen(AdministrationAccessDeniedPage.class)).isTrue();
        assertThat(magOpenen(AdministrationInternalErrorPage.class)).isTrue();
        assertThat(magOpenen(AdministrationPageExpiredPage.class)).isTrue();
    }

    @Test
    void babsZietDeStartpagina() {
        alsGebruikerMetRol(Roles.BABS);

        tester.startPage(BabsAdministrationPage.class);

        tester.assertRenderedPage(BabsAdministrationPage.class);
    }

    @Test
    void babsZietGeenLinksEnActiesVoorBeheer() {
        alsGebruikerMetRol(Roles.BABS);

        tester.startPage(BabsAdministrationPage.class);

        tester.assertInvisible(pad("nieuwBabsLink"));
        tester.assertInvisible(pad("importForm"));
        assertThat(tester.getLastResponseAsString())
                .doesNotContain("Acties", "Trouwlocaties", "Huwelijkstypen", "Dossiers", "Trouwboekjes");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("medewerkersrollen")
    void medewerkerZietAlleLinksEnActies(String rol) {
        alsGebruikerMetRol(rol);

        tester.startPage(BabsAdministrationPage.class);

        tester.assertVisible(pad("nieuwBabsLink"));
        tester.assertVisible(pad("importForm"));
        assertThat(tester.getLastResponseAsString())
                .contains("Acties", "Trouwlocaties", "Huwelijkstypen", "Dossiers", "Trouwboekjes");
    }

    @Test
    void babsKrijgtToegangGeweigerdOpAndereBeheerpagina() {
        alsGebruikerMetRol(Roles.BABS);
        // Laat de exception door de exception mapper afhandelen, zoals in productie
        tester.setExposeExceptions(false);

        tester.executeUrl("beheer/babs/nieuw");

        // Welke variant van de foutpagina (beheer of burger) de mapper kiest, test AdministrationErrorPageTest:
        // WicketTester zet /context/servlet voor het pad, waardoor dit request niet als /beheer-request telt.
        assertThat(tester.getLastResponse().getStatus()).isEqualTo(403);
        assertThat(tester.getLastRenderedPage()).isNotInstanceOf(BabsCreatePage.class);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("medewerkersrollen")
    void medewerkerZietBeheerpagina(String rol) {
        alsGebruikerMetRol(rol);

        tester.startPage(DossierAdministrationPage.class);

        tester.assertRenderedPage(DossierAdministrationPage.class);
    }

    @Test
    void babsMagDossierJsonNietOpvragen() {
        alsGebruikerMetRol(Roles.BABS);

        tester.executeUrl("beheer/dossiers/" + UUID.randomUUID() + "/json");

        assertThat(tester.getLastResponse().getStatus()).isEqualTo(403);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("medewerkersrollen")
    void medewerkerMagDossierJsonOpvragen(String rol) {
        alsGebruikerMetRol(rol);

        tester.executeUrl("beheer/dossiers/" + UUID.randomUUID() + "/json");

        // Toegang toegestaan; het dossier bestaat alleen niet
        assertThat(tester.getLastResponse().getStatus()).isEqualTo(404);
    }

    /** Spring Security dwingt nog steeds inloggen af; de rollen controleert Wicket. */
    @Test
    void nietIngelogdGaatNaarInlogpagina() {
        // TestRestTemplate volgt de redirect naar /login
        ResponseEntity<String> response = restTemplate.getForEntity("/beheer", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("action=\"/login\"");
    }

    private boolean magOpenen(Class<?> pagina) {
        IAuthorizationStrategy strategy = application.getSecuritySettings().getAuthorizationStrategy();
        return strategy.isInstantiationAuthorized(pagina.asSubclass(org.apache.wicket.Page.class));
    }

    private static void alsGebruikerMetRol(String rol) {
        SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
                "medewerker", null, List.of(new SimpleGrantedAuthority("ROLE_" + rol))));
    }
}

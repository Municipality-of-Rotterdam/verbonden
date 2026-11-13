package nl.rotterdam.verbonden.core.features.marriage_intake.ui;

import nl.rotterdam.verbonden.core.integration_test.BaseWicketTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.http.client.HttpRedirects;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;

import static org.assertj.core.api.Assertions.assertThat;

class MarriageIntakePageTest extends BaseWicketTest {

    @Autowired
    private TestRestTemplate restTemplate;

    /**
     * WicketTester bypasses the Spring Security HTTP filter chain, so this test renders the
     * page directly. The {@code @WithMockUser} annotation establishes an authenticated
     * Spring Security context, making the intent explicit: this page is for logged-in citizens.
     */
    @Test
    @WithMockUser(username = "999990007")
    void testRender() {
        tester.startPage(MarriageIntakePage.class);
        tester.assertRenderedPage(MarriageIntakePage.class);
    }

    @Test
    @WithMockUser(username = "999990007")
    void registratieEnCeremoniekeuzeHebbenEigenKaartZonderOmvattendeRand() {
        tester.startPage(MarriageIntakePage.class);

        String html = tester.getLastResponseAsString();
        assertThat(html)
                .contains("rd-intake-content--unbordered")
                .contains("rd-intake-step-layout--first-step")
                .contains("rd-ceremonie-kaart");
        assertThat(html.split("class=\"rd-card", -1)).hasSize(3);
    }

    /**
     * De namen en kenmerken van de ceremonie komen uit de database.
     */
    @Test
    @WithMockUser(username = "999990007")
    void toontDeNieuweNamenEnKenmerkenVanDeCeremonie() {
        tester.startPage(MarriageIntakePage.class);

        assertThat(tester.getLastResponseAsString())
                .contains("Gratis")
                .contains("U kiest zelf een datum. De ceremonie is op maandagochtend.")
                .contains("De ceremonie is in de felicitatiezaal in het stadhuis.")
                .contains("U neemt 2 getuigen mee. Andere gasten zijn niet welkom.")
                .contains("U kiest de trouwambtenaar niet zelf.")
                .contains("De trouwambtenaar houdt geen toespraak.")
                .contains("De ceremonie duurt 10 minuten.")
                .contains("Eenvoudig")
                .contains("U kiest zelf een datum uit de beschikbare data.")
                .contains("Er zijn maximaal 16 personen, inclusief u en uw partner.")
                .contains("De trouwambtenaar houdt een toespraak.")
                .contains("De ceremonie duurt 15 minuten.")
                .contains("Standaard")
                .contains("U kiest een trouwzaal in het stadhuis of een eigen locatie.")
                .contains("Het aantal personen hangt af van de locatie die u kiest.")
                .contains("U kunt uw voorkeur voor een trouwambtenaar aangeven.")
                .contains("De ceremonie duurt 30 minuten tot 1 uur.")
                .doesNotContain("Middelgroot", "Groot");
    }

    /**
     * De inleidende tekst staat in één property en gebruikt {@code <br/>} voor regelafbrekingen.
     */
    @Test
    @WithMockUser(username = "999990007")
    void rendertIntroInDrieRegels() {
        tester.startPage(MarriageIntakePage.class);
        assertThat(tester.getLastResponseAsString())
                .containsPattern("Gefeliciteerd!\\s*<br/>\\s*Hier doen jullie")
                .containsPattern("ceremonie\\.\\s*<br/>\\s*Zoiets doet u");
    }

    /**
     * De Stencil-iconen lezen de CSP-nonce uit {@code <meta name="csp-nonce">} voor hun eigen
     * {@code <style>}-blokken; zonder die tag blokkeert de browser ze.
     */
    @Test
    @WithMockUser(username = "999990007")
    void rendertCspNonceMetaTagVoorStencilIconen() {
        tester.startPage(MarriageIntakePage.class);
        assertThat(tester.getLastResponseAsString()).containsPattern("<meta name=\"csp-nonce\" content=\"[^\"]+\"");
    }

    /**
     * Verifies that an unauthenticated HTTP request to the home page is redirected to the
     * citizen login page ({@code /inloggen}). This test exercises the real Spring Security
     * filter chain, unlike the WicketTester-based test above.
     */
    @Test
    void unauthenticatedRequestRedirectsToLogin() {
        ResponseEntity<String> response = restTemplate.withRedirects(HttpRedirects.DONT_FOLLOW).getForEntity("/", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(response.getHeaders().getLocation()).hasPath("/inloggen");
    }
}

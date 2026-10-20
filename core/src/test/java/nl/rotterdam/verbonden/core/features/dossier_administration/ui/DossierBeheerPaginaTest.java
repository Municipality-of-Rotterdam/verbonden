package nl.rotterdam.verbonden.core.features.dossier_administration.ui;

import nl.rotterdam.verbonden.core.domain.BuitenlandsPersoonsnummer;
import nl.rotterdam.verbonden.core.domain.BurgerServiceNummer;
import nl.rotterdam.verbonden.core.features.dossier_administration.application.DossierAdministrationService;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.CreateBalieDossierDto;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.DossierDetailDto;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.InzageActie;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.PersoonsgegevensDto;
import nl.rotterdam.verbonden.core.features.dossier_administration.repository.DossierInzageRepository;
import nl.rotterdam.verbonden.core.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.AanmaakKanaal;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.RegistratieType;
import nl.rotterdam.verbonden.core.identity.Roles;
import nl.rotterdam.verbonden.core.integration_test.BaseWicketTest;
import org.apache.wicket.util.file.File;
import org.apache.wicket.util.tester.FormTester;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.http.client.HttpRedirects;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@WithMockUser(username = "medewerker", roles = Roles.BEHEER)
class DossierBeheerPaginaTest extends BaseWicketTest {

    private static final BurgerServiceNummer BSN_PARTNER_1 = new BurgerServiceNummer("999990007");

    @Autowired
    private DossierAdministrationService dossierAdministrationService;

    @Autowired
    private MarriageIntakeService marriageIntakeService;

    @Autowired
    private DossierInzageRepository dossierInzageRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @TempDir
    private Path tempDir;

    @Test
    void detailPagina_toontAlleGegevensEnLegtInzageVast() {
        UUID dossierId = maakDossierMetPartnerZonderBsn();

        tester.startPage(DossierDetailPage.class, DossierBeheerBasePage.parametersVoor(dossierId));

        tester.assertRenderedPage(DossierDetailPage.class);
        assertThat(tester.getLastResponseAsString())
                .contains("Van Muiswinkel")
                .contains("999990007")
                .contains("Smith")
                .contains("123-45-6789")
                .contains("Persoonsnummer (paspoort)")
                .contains("Gecontroleerd door medewerker")
                .contains("Balie")
                .contains("Klein");
        assertThat(dossierInzageRepository.findByDossierUuidAndActie(dossierId, InzageActie.DOSSIER_BEKEKEN))
                .isNotEmpty();
    }

    @Test
    void json_bevatAlleGegevensEnLegtInzageVast() {
        UUID dossierId = maakDossierMetPartnerZonderBsn();
        tester.startPage(DossierDetailPage.class, DossierBeheerBasePage.parametersVoor(dossierId));
        tester.assertContains("href=\"./" + dossierId + "/json\"");
        int inzagesVoor = dossierInzageRepository.findByDossierUuidAndActie(dossierId, InzageActie.DOSSIER_BEKEKEN).size();

        tester.executeUrl("beheer/dossiers/" + dossierId + "/json");

        assertThat(tester.getLastResponse().getContentType()).startsWith("application/json");
        assertThat(tester.getLastResponseAsString())
                .contains("\"dossierId\" : \"" + dossierId + "\"")
                // Value types als hun waarde, niet als {"value": ...}
                .contains("\"bsn\" : \"999990007\"")
                .contains("\"buitenlandsPersoonsnummer\" : \"123-45-6789\"")
                .contains("\"achternaam\" : \"Smith\"")
                .contains("\"ceremonieSoort\" : \"KLEIN\"");
        assertThat(dossierInzageRepository.findByDossierUuidAndActie(dossierId, InzageActie.DOSSIER_BEKEKEN))
                .hasSize(inzagesVoor + 1);
    }

    @Test
    void json_onbekendDossier_geeft404() {
        tester.executeUrl("beheer/dossiers/" + UUID.randomUUID() + "/json");

        assertThat(tester.getLastResponse().getStatus()).isEqualTo(404);
    }

    /** WicketTester slaat de Spring Security filter chain over; dit gaat via een echt HTTP-request. */
    @Test
    void json_zonderInloggen_isNietBereikbaar() {
        ResponseEntity<String> response = restTemplate.withRedirects(HttpRedirects.DONT_FOLLOW)
                .getForEntity("/beheer/dossiers/" + UUID.randomUUID() + "/json", String.class);

        assertThat(response.getStatusCode().is3xxRedirection()).isTrue();
        assertThat(response.getHeaders().getLocation()).hasPath("/login");
    }

    @Test
    void detailPagina_onbekendDossier_gaatTerugNaarOverzicht() {
        tester.startPage(DossierDetailPage.class, DossierBeheerBasePage.parametersVoor(UUID.randomUUID()));

        tester.assertRenderedPage(DossierAdministrationPage.class);
    }

    @Test
    void detailPagina_bestandToevoegen() throws IOException {
        UUID dossierId = maakDossierMetPartnerZonderBsn();
        Path pdf = tempDir.resolve("paspoort.pdf");
        Files.writeString(pdf, "%PDF-1.7 paspoort", StandardCharsets.US_ASCII);

        tester.startPage(DossierDetailPage.class, DossierBeheerBasePage.parametersVoor(dossierId));
        FormTester form = formTester("bestandToevoegenForm");
        form.setFile(veld("bestandToevoegenForm", "bestand"), new File(pdf.toFile()), "application/pdf");
        tester.addRequestHeader("sec-fetch-site", "same-origin");
        form.submit(veld("bestandToevoegenForm", "toevoegen"));

        tester.assertRenderedPage(DossierDetailPage.class);
        assertThat(dossierAdministrationService.findDetail(dossierId).bestanden())
                .singleElement()
                .satisfies(b -> assertThat(b.bestandsnaam()).isEqualTo("paspoort.pdf"));
        assertThat(tester.getLastResponseAsString()).contains("paspoort.pdf");
    }

    @Test
    void detailPagina_nietToegestaanBestand_toontMelding() throws IOException {
        UUID dossierId = maakDossierMetPartnerZonderBsn();
        Path exe = tempDir.resolve("programma.exe");
        Files.writeString(exe, "MZ", StandardCharsets.US_ASCII);

        tester.startPage(DossierDetailPage.class, DossierBeheerBasePage.parametersVoor(dossierId));
        FormTester form = formTester("bestandToevoegenForm");
        form.setFile(veld("bestandToevoegenForm", "bestand"), new File(exe.toFile()), "application/octet-stream");
        tester.addRequestHeader("sec-fetch-site", "same-origin");
        form.submit(veld("bestandToevoegenForm", "toevoegen"));

        assertThat(tester.getLastResponseAsString()).contains("Dit soort bestand is niet toegestaan");
        assertThat(dossierAdministrationService.findDetail(dossierId).bestanden()).isEmpty();
    }

    @Test
    void aanmaakPagina_maaktDossierAanVoorPartnerZonderBsn() {
        tester.startPage(DossierCreatePage.class);

        // Partner 2 heeft geen BSN: de velden voor de persoonsgegevens verschijnen
        FormTester toggle = formTester("dossierForm");
        toggle.setValue(veld("dossierForm", "partner2ZonderBsn"), true);
        tester.addRequestHeader("sec-fetch-site", "same-origin");
        tester.executeAjaxEvent(tester.getComponentFromLastRenderedPage(
                pad("dossierForm") + ":" + veld("dossierForm", "partner2ZonderBsn")), "change");

        FormTester form = formTester("dossierForm");
        form.select(veld("dossierForm", "kanaal"), 1);
        form.select(veld("dossierForm", "registratieType"), 0);
        form.select(veld("dossierForm", "ceremonieSoort"), 0);
        form.setValue(veld("dossierForm", "bsnPartner1"), "999990007");
        form.setValue(veld("dossierForm", "identiteitPartner1Gecontroleerd"), true);
        form.setValue(veld("dossierForm", "persoonsnummer"), "123-45-6789");
        form.setValue(veld("dossierForm", "achternaam"), "Smith");
        form.setValue(veld("dossierForm", "voornamen"), "John");
        form.setValue(veld("dossierForm", "geboortedatum"), "1990-04-01");
        form.setValue(veld("dossierForm", "nationaliteit"), "Amerikaanse");
        form.setValue(veld("dossierForm", "identiteitPartner2Gecontroleerd"), true);
        tester.addRequestHeader("sec-fetch-site", "same-origin");
        form.submit(veld("dossierForm", "aanmaken"));

        tester.assertNoErrorMessage();
        tester.assertRenderedPage(DossierDetailPage.class);
        UUID dossierId = marriageIntakeService.findDossierIdByBsn(BSN_PARTNER_1).orElseThrow();
        DossierDetailDto detail = dossierAdministrationService.findDetail(dossierId);
        assertThat(detail.kanaal()).isEqualTo(AanmaakKanaal.VIDEO);
        assertThat(detail.partners()).hasSize(2);
        assertThat(detail.partners().get(1).gegevens().buitenlandsPersoonsnummer())
                .isEqualTo(new BuitenlandsPersoonsnummer("123-45-6789"));
    }

    @Test
    void aanmaakPagina_zonderIdentiteitscontrole_maaktGeenDossierAan() {
        tester.startPage(DossierCreatePage.class);

        FormTester form = formTester("dossierForm");
        form.select(veld("dossierForm", "kanaal"), 0);
        form.select(veld("dossierForm", "registratieType"), 0);
        form.select(veld("dossierForm", "ceremonieSoort"), 0);
        form.setValue(veld("dossierForm", "bsnPartner1"), "999990007");
        form.setValue(veld("dossierForm", "bsnPartner2"), "999990019");
        tester.addRequestHeader("sec-fetch-site", "same-origin");
        form.submit(veld("dossierForm", "aanmaken"));

        tester.assertRenderedPage(DossierCreatePage.class);
        assertThat(tester.getLastResponseAsString()).contains("Controleer de identiteit van beide partners");
        assertThat(marriageIntakeService.findDossierIdByBsn(BSN_PARTNER_1)).isEmpty();
    }

    private UUID maakDossierMetPartnerZonderBsn() {
        return dossierAdministrationService.create(new CreateBalieDossierDto(AanmaakKanaal.BALIE,
                RegistratieType.HUWELIJK, CeremonieSoort.KLEIN, BSN_PARTNER_1, null,
                new BuitenlandsPersoonsnummer("123-45-6789"),
                new PersoonsgegevensDto("Smith", "John", LocalDate.of(1990, 4, 1), "Boston", "Amerikaanse", "Ongehuwd")));
    }
}

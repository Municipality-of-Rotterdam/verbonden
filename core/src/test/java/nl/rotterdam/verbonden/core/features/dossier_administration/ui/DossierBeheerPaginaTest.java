package nl.rotterdam.verbonden.core.features.dossier_administration.ui;

import nl.rotterdam.verbonden.core.domain.BuitenlandsPersoonsnummer;
import nl.rotterdam.verbonden.core.domain.BurgerServiceNummer;
import nl.rotterdam.verbonden.core.domain.Postcode;
import nl.rotterdam.verbonden.core.features.dossier_administration.application.DossierAdministrationService;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.ChangeGetuigeDto;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.CreateBalieDossierDto;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.DetailGetuigeDto;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.CreateDossierNotitieDto;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.DossierDetailDto;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.InzageActie;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.ListDossierNotitieDto;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.PersoonsgegevensDto;
import nl.rotterdam.verbonden.core.features.dossier_administration.repository.DossierInzageRepository;
import nl.rotterdam.verbonden.core.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.AanmaakKanaal;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.RegistratieType;
import nl.rotterdam.verbonden.core.identity.Roles;
import nl.rotterdam.verbonden.core.integration_test.BaseWicketTest;
import nl.rotterdam.verbonden.core.integration_test.CompleetDossierTestData;
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
import java.time.format.DateTimeFormatter;
import java.util.List;
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

    @Autowired
    private CompleetDossierTestData testData;

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
    void json_bevatAlleGegevensVanDeGetuigen() {
        UUID dossierId = testData.maakConceptDossier();
        dossierAdministrationService.updateGetuigen(dossierId, List.of(
                new ChangeGetuigeDto(1, "Kwik van Willegenburgh", "Kwik Donald", "van Willegenburgh",
                        LocalDate.of(1990, 4, 1), new Postcode("b-1000"), "Brussel", "België")));

        tester.executeUrl("beheer/dossiers/" + dossierId + "/json");

        assertThat(tester.getLastResponseAsString())
                .contains("\"volgnummer\" : 1")
                .contains("\"naam\" : \"Kwik van Willegenburgh\"")
                .contains("\"voornamen\" : \"Kwik Donald\"")
                .contains("\"achternaam\" : \"van Willegenburgh\"")
                .contains("\"geboortedatum\" : \"1990-04-01\"")
                // Value type als zijn waarde, niet als {"value": ...}
                .contains("\"postcode\" : \"B-1000\"")
                .contains("\"woonplaats\" : \"Brussel\"")
                .contains("\"land\" : \"België\"");
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
    void getuigenPagina_slaatPersoonsgegevensOpEnToontZeOpDeDetailpagina() {
        UUID dossierId = testData.maakCompleetDossier();
        tester.startPage(DossierGetuigenUpdatePage.class, DossierBeheerBasePage.parametersVoor(dossierId));
        assertPaginaSerialiseerbaar();

        FormTester form = formTester("getuigenForm");
        form.setValue(veld("getuigenForm", "voornamen"), "Kwik Donald");
        form.setValue(veld("getuigenForm", "achternaam"), "van Willegenburgh");
        form.setValue(veld("getuigenForm", "geboortedatum"), "1990-04-01");
        form.setValue(veld("getuigenForm", "postcode"), "3011ad");
        form.setValue(veld("getuigenForm", "woonplaats"), "Rotterdam");
        tester.addRequestHeader("sec-fetch-site", "same-origin");
        form.submit(veld("getuigenForm", "opslaan"));

        tester.assertNoErrorMessage();
        tester.assertRenderedPage(DossierDetailPage.class);
        assertThat(tester.getLastResponseAsString())
                .doesNotContain("jonger dan 18 jaar")
                .contains("Kwik Donald")
                .contains("01-04-1990")
                .contains("3011 AD");
        assertThat(dossierAdministrationService.findDetail(dossierId).getuigen())
                .first()
                .isEqualTo(new DetailGetuigeDto(1, "Kwik van Willegenburgh", "Kwik Donald", "van Willegenburgh",
                        LocalDate.of(1990, 4, 1), new Postcode("3011 AD"), "Rotterdam", "Nederland"));
    }

    @Test
    void getuigenPagina_buitenlandsAdres_wordtOpgeslagen() {
        UUID dossierId = testData.maakCompleetDossier();
        tester.startPage(DossierGetuigenUpdatePage.class, DossierBeheerBasePage.parametersVoor(dossierId));

        FormTester form = formTester("getuigenForm");
        form.setValue(veld("getuigenForm", "postcode"), "b-1000");
        form.setValue(veld("getuigenForm", "woonplaats"), "Brussel");
        form.setValue(veld("getuigenForm", "land"), "België");
        tester.addRequestHeader("sec-fetch-site", "same-origin");
        form.submit(veld("getuigenForm", "opslaan"));

        tester.assertNoErrorMessage();
        tester.assertRenderedPage(DossierDetailPage.class);
        assertThat(dossierAdministrationService.findDetail(dossierId).getuigen())
                .first()
                .extracting(DetailGetuigeDto::postcode, DetailGetuigeDto::land)
                .containsExactly(new Postcode("B-1000"), "België");
    }

    @Test
    void getuigenPagina_inNederlandMetBuitenlandsePostcode_slaatNietOp() {
        UUID dossierId = testData.maakCompleetDossier();
        tester.startPage(DossierGetuigenUpdatePage.class, DossierBeheerBasePage.parametersVoor(dossierId));

        // Het land staat standaard op Nederland
        FormTester form = formTester("getuigenForm");
        form.setValue(veld("getuigenForm", "postcode"), "B-1000");
        tester.addRequestHeader("sec-fetch-site", "same-origin");
        form.submit(veld("getuigenForm", "opslaan"));

        tester.assertRenderedPage(DossierGetuigenUpdatePage.class);
        tester.assertErrorMessages(
                "Getuige 1: geef een Nederlandse postcode op (bijv. 3011 AD) of kies een ander land.");
        assertThat(dossierAdministrationService.findDetail(dossierId).getuigen())
                .extracting(DetailGetuigeDto::postcode)
                .containsOnlyNulls();
    }

    @Test
    void getuigenPagina_ongeldigePostcode_slaatNietOp() {
        UUID dossierId = testData.maakCompleetDossier();
        tester.startPage(DossierGetuigenUpdatePage.class, DossierBeheerBasePage.parametersVoor(dossierId));

        FormTester form = formTester("getuigenForm");
        form.setValue(veld("getuigenForm", "postcode"), "3011 A?");
        tester.addRequestHeader("sec-fetch-site", "same-origin");
        form.submit(veld("getuigenForm", "opslaan"));

        tester.assertRenderedPage(DossierGetuigenUpdatePage.class);
        assertThat(tester.getLastResponseAsString()).contains("Geef een geldige postcode op");
        assertThat(dossierAdministrationService.findDetail(dossierId).getuigen())
                .extracting(DetailGetuigeDto::postcode)
                .containsOnlyNulls();
    }

    @Test
    void getuigenPagina_getuigeJongerDan18OpCeremonie_slaatOpMetWaarschuwing() {
        UUID dossierId = testData.maakCompleetDossier();
        LocalDate ceremonie = dossierAdministrationService.findDetail(dossierId).datumTijdHuwelijk().toLocalDate();
        LocalDate geboortedatum = ceremonie.minusYears(18).plusDays(1);

        slaGeboortedatumEersteGetuigeOp(dossierId, geboortedatum);

        tester.assertRenderedPage(DossierDetailPage.class);
        assertThat(tester.getLastResponseAsString()).contains("Getuige 1 is op de dag van de ceremonie ("
                + ceremonie.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) + ") jonger dan 18 jaar.");
        assertThat(dossierAdministrationService.findDetail(dossierId).getuigen())
                .first()
                .extracting(DetailGetuigeDto::geboortedatum)
                .isEqualTo(geboortedatum);
    }

    @Test
    void getuigenPagina_getuigeOpCeremonie18Jaar_geeftGeenWaarschuwing() {
        UUID dossierId = testData.maakCompleetDossier();
        LocalDate ceremonie = dossierAdministrationService.findDetail(dossierId).datumTijdHuwelijk().toLocalDate();

        slaGeboortedatumEersteGetuigeOp(dossierId, ceremonie.minusYears(18));

        tester.assertRenderedPage(DossierDetailPage.class);
        assertThat(tester.getLastResponseAsString()).doesNotContain("jonger dan 18 jaar");
    }

    private void slaGeboortedatumEersteGetuigeOp(UUID dossierId, LocalDate geboortedatum) {
        tester.startPage(DossierGetuigenUpdatePage.class, DossierBeheerBasePage.parametersVoor(dossierId));
        FormTester form = formTester("getuigenForm");
        form.setValue(veld("getuigenForm", "geboortedatum"), geboortedatum.toString());
        tester.addRequestHeader("sec-fetch-site", "same-origin");
        form.submit(veld("getuigenForm", "opslaan"));
    void detailPagina_notitiesToevoegenEnNieuwsteEerstWeergeven() {
        UUID dossierId = maakDossierMetPartnerZonderBsn();
        dossierAdministrationService.createNotitie(dossierId, new CreateDossierNotitieDto("Oudste notitie"));
        dossierAdministrationService.createNotitie(dossierId, new CreateDossierNotitieDto("Bestaande notitie"));

        tester.startPage(DossierDetailPage.class, DossierBeheerBasePage.parametersVoor(dossierId));

        String response = tester.getLastResponseAsString();
        assertThat(response).contains("rd-notitie-tekst");
        assertThat(response).contains("Bestaande notitie", "Oudste notitie");
        assertThat(response).contains("maxlength=\"1000\"");
        assertThat(response).contains("rd-notitie-formulier", "rows=\"8\"");
        assertThat(response).contains("<h2>E-mails</h2>", "<h2>Notities</h2>");
        assertThat(response.indexOf("<h2>E-mails</h2>")).isLessThan(response.indexOf("<h2>Notities</h2>"));
        String notities = response.substring(response.indexOf("<h2>Notities</h2>"));
        assertThat(notities).contains("<table class=\"utrecht-table rd-notities-tabel\">", "utrecht-table__header-cell",
                "utrecht-table__body", "utrecht-table__row", "utrecht-table__cell");
        String nieuwsteNotitie = pad("notities") + ":0";
        tester.assertLabel(nieuwsteNotitie + ":auteur", "medewerker");
        tester.assertLabel(nieuwsteNotitie + ":tekst", "Bestaande notitie");
        assertThat(tester.getComponentFromLastRenderedPage(nieuwsteNotitie + ":aangemaakt")
                .getDefaultModelObjectAsString()).isNotBlank();
        assertThat(response.indexOf("Bestaande notitie")).isLessThan(response.indexOf("Oudste notitie"));

        FormTester form = formTester("notitieToevoegenForm");
        form.setValue(veld("notitieToevoegenForm", "notitie"), "Nieuwe notitie");
        tester.addRequestHeader("sec-fetch-site", "same-origin");
        form.submit(veld("notitieToevoegenForm", "toevoegen"));

        tester.assertRenderedPage(DossierDetailPage.class);
        assertThat(dossierAdministrationService.findNotities(dossierId))
                .extracting(ListDossierNotitieDto::tekst)
                .containsExactly("Nieuwe notitie", "Bestaande notitie", "Oudste notitie");
        assertThat(tester.getLastResponseAsString()).contains("Nieuwe notitie");
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

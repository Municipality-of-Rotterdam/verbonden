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
import nl.rotterdam.verbonden.core.integration_test.BaseWicketTest;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.util.file.File;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.visit.IVisit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
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
@WithMockUser(username = "medewerker", roles = "BEHEERDER")
class DossierBeheerPaginaTest extends BaseWicketTest {

    private static final BurgerServiceNummer BSN_PARTNER_1 = new BurgerServiceNummer("999990007");

    @Autowired
    private DossierAdministrationService dossierAdministrationService;

    @Autowired
    private MarriageIntakeService marriageIntakeService;

    @Autowired
    private DossierInzageRepository dossierInzageRepository;

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
    void detailPagina_onbekendDossier_gaatTerugNaarOverzicht() {
        tester.startPage(DossierDetailPage.class, DossierBeheerBasePage.parametersVoor(UUID.randomUUID()));

        tester.assertRenderedPage(DossierAdministrationPage.class);
    }

    @Test
    void detailPagina_bestandToevoegen() throws IOException {
        UUID dossierId = maakDossierMetPartnerZonderBsn();
        Path pdf = tempDir.resolve("paspoort.pdf");
        Files.write(pdf, "%PDF-1.7 paspoort".getBytes(StandardCharsets.US_ASCII));

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
        Files.write(exe, "MZ".getBytes(StandardCharsets.US_ASCII));

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

    private FormTester formTester(String formId) {
        return tester.newFormTester(pad(formId));
    }

    /** Het pad van een component op de laatst getoonde pagina, op basis van zijn wicket:id. */
    private String pad(String id) {
        Component component = tester.getLastRenderedPage().visitChildren(
                (Component c, IVisit<Component> visit) -> {
                    if (c.getId().equals(id)) {
                        visit.stop(c);
                    }
                });
        return component.getPageRelativePath();
    }

    /**
     * Het pad, relatief aan het formulier, van het invoerelement binnen het formulierveld met deze wicket:id.
     */
    private String veld(String formId, String id) {
        Form<?> form = (Form<?>) tester.getComponentFromLastRenderedPage(pad(formId));
        Component veld = form.visitChildren((Component c, IVisit<Component> visit) -> {
            if (c.getId().equals(id)) {
                visit.stop(c);
            }
        });
        Component invoer = veld instanceof FormComponent<?> || !(veld instanceof MarkupContainer container)
                ? veld
                : container.visitChildren(FormComponent.class, (FormComponent<?> c, IVisit<Component> visit) -> visit.stop(c));
        if (invoer == null) {
            invoer = veld;
        }
        return invoer.getPageRelativePath().substring(form.getPageRelativePath().length() + 1);
    }
}

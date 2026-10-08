package nl.rotterdam.verbonden.core.features.dossier_administration.application;

import nl.rotterdam.verbonden.core.domain.BuitenlandsPersoonsnummer;
import nl.rotterdam.verbonden.core.domain.BurgerServiceNummer;
import nl.rotterdam.verbonden.core.domain.Telefoonnummer;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.BestandNietToegestaanException;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.ChangeExtrasDto;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.ChangePartnerDto;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.CreateBalieDossierDto;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.CreateDossierBestandDto;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.DetailPartnerDto;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.DossierBestandDto;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.DossierBestandType;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.DossierDetailDto;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.InzageActie;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.PartnerHeeftAlDossierException;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.PartnerNietInBrpException;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.PersoonsgegevensDto;
import nl.rotterdam.verbonden.core.features.dossier_administration.repository.DossierInzageRepository;
import nl.rotterdam.verbonden.core.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.AanmaakKanaal;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierAccessOutcome;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierStatus;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.PartnerGegevensDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.RegistratieType;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.SaveGetuigenDto;
import nl.rotterdam.verbonden.core.integration_test.CompleetDossierTestData;
import nl.rotterdam.verbonden.core.integration_test.VerbondenIntegrationTest;
import nl.rotterdam.verbonden.core.persistence.DossierInzageEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@VerbondenIntegrationTest
@Transactional
@WithMockUser(username = "medewerker", roles = "BEHEERDER")
class DossierBeheerIntegrationTest {

    private static final BurgerServiceNummer BSN_PARTNER_1 = new BurgerServiceNummer("999990007");
    private static final BurgerServiceNummer BSN_PARTNER_2 = new BurgerServiceNummer("999990019");
    private static final BurgerServiceNummer BSN_NIET_IN_BRP = new BurgerServiceNummer("999990056");
    private static final BuitenlandsPersoonsnummer PERSOONSNUMMER = new BuitenlandsPersoonsnummer("123-45-6789");
    private static final PersoonsgegevensDto PERSOONSGEGEVENS = new PersoonsgegevensDto(
            "Smith", "John", LocalDate.of(1990, 4, 1), "Boston", "Amerikaanse", "Ongehuwd");

    @Autowired
    private DossierAdministrationService dossierAdministrationService;

    @Autowired
    private MarriageIntakeService marriageIntakeService;

    @Autowired
    private DossierInzageRepository dossierInzageRepository;

    @Autowired
    private CompleetDossierTestData testData;

    @Test
    void create_metTweeBsns_koppeltBeidePartnersEnLegtIdentiteitscontroleVast() {
        UUID dossierId = dossierAdministrationService.create(new CreateBalieDossierDto(AanmaakKanaal.BALIE,
                RegistratieType.HUWELIJK, CeremonieSoort.KLEIN, BSN_PARTNER_1, BSN_PARTNER_2, null, null));

        DossierDetailDto detail = dossierAdministrationService.findDetail(dossierId);
        assertThat(detail.kanaal()).isEqualTo(AanmaakKanaal.BALIE);
        assertThat(detail.aangemaaktDoor()).isEqualTo("medewerker");
        assertThat(detail.status()).isEqualTo(DossierStatus.CONCEPT);
        assertThat(detail.locatieNaam()).isNotNull();
        assertThat(detail.partners()).hasSize(2).allSatisfy(partner -> {
            assertThat(partner.identiteitGecontroleerdDoor()).isEqualTo("medewerker");
            assertThat(partner.identiteitGecontroleerdOp()).isNotNull();
        });
        assertThat(detail.partners()).extracting(p -> p.gegevens().achternaam())
                .containsExactly("Van Muiswinkel", "De Vries");

        // Beide partners kunnen zelf met DigiD verder, zonder uitnodiging
        assertThat(marriageIntakeService.resolveAccess(dossierId, BSN_PARTNER_2).scenario())
                .isEqualTo(DossierAccessOutcome.Scenario.GRANTED);
    }

    @Test
    void create_partnerZonderBsn_gebruiktIngevoerdePersoonsgegevens() {
        UUID dossierId = maakDossierMetPartnerZonderBsn();

        PartnerGegevensDto partner2 = partner(dossierId, 2);
        assertThat(partner2.bsn()).isNull();
        assertThat(partner2.buitenlandsPersoonsnummer()).isEqualTo(PERSOONSNUMMER);
        assertThat(partner2.achternaam()).isEqualTo("Smith");
        assertThat(partner2.geboortedatum()).isEqualTo(LocalDate.of(1990, 4, 1));

        // Partner 1 kan online verder, en ook de gegevens van partner 2 invullen
        assertThat(marriageIntakeService.resolveAccess(dossierId, BSN_PARTNER_1).scenario())
                .isEqualTo(DossierAccessOutcome.Scenario.GRANTED);
        marriageIntakeService.slaPartnerGegevensOp(dossierId, 2, "Smith - Van Muiswinkel");
        assertThat(partner(dossierId, 2).gekozenAchternaam()).isEqualTo("Smith - Van Muiswinkel");
    }

    @Test
    void create_bsnNietInBrp_wordtGeweigerd() {
        assertThatThrownBy(() -> dossierAdministrationService.create(new CreateBalieDossierDto(AanmaakKanaal.VIDEO,
                RegistratieType.HUWELIJK, CeremonieSoort.KLEIN, BSN_NIET_IN_BRP, BSN_PARTNER_2, null, null)))
                .isInstanceOf(PartnerNietInBrpException.class);
    }

    @Test
    void create_partnerMetBestaandDossier_wordtGeweigerd() {
        testData.maakConceptDossier();

        assertThatThrownBy(() -> dossierAdministrationService.create(new CreateBalieDossierDto(AanmaakKanaal.BALIE,
                RegistratieType.HUWELIJK, CeremonieSoort.KLEIN, BSN_PARTNER_1, CompleetDossierTestData.BSN_PARTNER_1,
                null, null)))
                .isInstanceOf(PartnerHeeftAlDossierException.class);
    }

    @Test
    void findDetail_legtInzageVast() {
        UUID dossierId = testData.maakConceptDossier();

        dossierAdministrationService.findDetail(dossierId);

        assertThat(dossierInzageRepository.findByDossierUuidAndActie(dossierId, InzageActie.DOSSIER_BEKEKEN))
                .singleElement()
                .extracting(DossierInzageEntity::getMedewerker)
                .isEqualTo("medewerker");
    }

    @Test
    void baliedossier_kanCompleetGemaaktEnIngediendWorden() {
        UUID dossierId = maakDossierMetPartnerZonderBsn();
        testData.maakBeschikbaarheidVoorKleinHuwelijk();
        LocalDateTime slot = marriageIntakeService.findAllBeschikbareSlots(dossierId).iterator().next();
        dossierAdministrationService.updateAfspraak(dossierId, slot.toLocalDate(), slot.toLocalTime());
        dossierAdministrationService.updateGetuigen(dossierId, List.of(
                new SaveGetuigenDto(1, "Kwik van Willegenburgh"),
                new SaveGetuigenDto(2, "Kwek van Willegenburgh")));
        for (int volgorde = 1; volgorde <= 2; volgorde++) {
            PartnerGegevensDto partner = partner(dossierId, volgorde);
            dossierAdministrationService.updatePartner(dossierId, new ChangePartnerDto(volgorde, partner.versie(),
                    "Smith", null, null,
                    partner.buitenlandsPersoonsnummer(), partner.bsn() == null ? PERSOONSGEGEVENS : null));
        }

        dossierAdministrationService.dienIn(dossierId);

        DossierDetailDto detail = dossierAdministrationService.findDetail(dossierId);
        assertThat(detail.status()).isEqualTo(DossierStatus.INGEDIEND);
        assertThat(detail.datumTijdHuwelijk()).isEqualTo(slot);
    }

    @Test
    void medewerker_kanIngediendDossierWijzigen() {
        UUID dossierId = testData.maakCompleetDossier();
        marriageIntakeService.dienIn(dossierId);

        dossierAdministrationService.updateGetuigen(dossierId, List.of(
                new SaveGetuigenDto(1, "Kwak van Willegenburgh"),
                new SaveGetuigenDto(2, "Kwek van Willegenburgh")));
        dossierAdministrationService.updateExtras(dossierId, new ChangeExtrasDto(true, false, null, true));

        DossierDetailDto detail = dossierAdministrationService.findDetail(dossierId);
        assertThat(detail.status()).isEqualTo(DossierStatus.INGEDIEND);
        assertThat(detail.getuigen()).extracting(g -> g.naam()).contains("Kwak van Willegenburgh");
        assertThat(detail.extraKeuzes().ringenUitwisselen()).isTrue();
        assertThat(detail.extraKeuzes().internationaleAkte()).isTrue();
        // De prijs van de akte ligt vast op het tarief van de indieningsdatum
        assertThat(marriageIntakeService.findInternationaleAktePrijs(dossierId)).isNotNull();
    }

    @Test
    void updatePartner_metVerouderdeVersie_wordtGeweigerd() {
        UUID dossierId = maakDossierMetPartnerZonderBsn();
        long versie = partner(dossierId, 1).versie();
        dossierAdministrationService.updatePartner(dossierId, new ChangePartnerDto(1, versie, null,
                new Telefoonnummer("0612345678"), null, null, null));

        assertThatThrownBy(() -> dossierAdministrationService.updatePartner(dossierId, new ChangePartnerDto(1, versie,
                null, new Telefoonnummer("0687654321"), null, null, null)))
                .isInstanceOf(org.springframework.dao.OptimisticLockingFailureException.class);
    }

    @Test
    void updatePartner_persoonsgegevensVanPartnerMetBsnKomenUitDeBrp() {
        UUID dossierId = maakDossierMetPartnerZonderBsn();

        assertThatThrownBy(() -> dossierAdministrationService.updatePartner(dossierId, new ChangePartnerDto(1,
                partner(dossierId, 1).versie(), null, null, null, null, PERSOONSGEGEVENS)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void bestand_toevoegenDownloadenEnVerwijderen() {
        UUID dossierId = testData.maakConceptDossier();
        byte[] pdf = "%PDF-1.7 paspoort".getBytes(StandardCharsets.US_ASCII);

        long bestandId = dossierAdministrationService.createBestand(dossierId,
                new CreateDossierBestandDto("C:\\Scans\\paspoort.pdf", pdf));

        assertThat(dossierAdministrationService.findDetail(dossierId).bestanden()).singleElement().satisfies(b -> {
            assertThat(b.bestandsnaam()).isEqualTo("paspoort.pdf");
            assertThat(b.bestandType()).isEqualTo(DossierBestandType.PDF);
            assertThat(b.toegevoegdDoor()).isEqualTo("medewerker");
        });

        DossierBestandDto bestand = dossierAdministrationService.findBestand(dossierId, bestandId);
        assertThat(bestand.inhoud()).isEqualTo(pdf);
        assertThat(dossierInzageRepository.findByDossierUuidAndActie(dossierId, InzageActie.BESTAND_GEDOWNLOAD))
                .hasSize(1);

        dossierAdministrationService.deleteBestand(dossierId, bestandId);
        assertThat(dossierAdministrationService.findDetail(dossierId).bestanden()).isEmpty();
    }

    @Test
    void bestand_nietToegestaanOfHernoemd_wordtGeweigerd() {
        UUID dossierId = testData.maakConceptDossier();

        assertThatThrownBy(() -> dossierAdministrationService.createBestand(dossierId,
                new CreateDossierBestandDto("programma.exe", "MZ".getBytes(StandardCharsets.US_ASCII))))
                .isInstanceOf(BestandNietToegestaanException.class)
                .hasMessageContaining("niet toegestaan");
        assertThatThrownBy(() -> dossierAdministrationService.createBestand(dossierId,
                new CreateDossierBestandDto("hernoemd.pdf", "PK\u0003\u0004".getBytes(StandardCharsets.ISO_8859_1))))
                .isInstanceOf(BestandNietToegestaanException.class)
                .hasMessageContaining("past niet bij de extensie");
    }

    @Test
    void bestand_vanAnderDossier_kanNietWordenOpgevraagd() {
        UUID dossierId = testData.maakConceptDossier();
        long bestandId = dossierAdministrationService.createBestand(dossierId,
                new CreateDossierBestandDto("paspoort.pdf", "%PDF-1.7".getBytes(StandardCharsets.US_ASCII)));
        UUID anderDossier = maakDossierMetPartnerZonderBsn();

        assertThatThrownBy(() -> dossierAdministrationService.findBestand(anderDossier, bestandId))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private UUID maakDossierMetPartnerZonderBsn() {
        return dossierAdministrationService.create(new CreateBalieDossierDto(AanmaakKanaal.BALIE,
                RegistratieType.HUWELIJK, CeremonieSoort.KLEIN, BSN_PARTNER_1, null, PERSOONSNUMMER, PERSOONSGEGEVENS));
    }

    private PartnerGegevensDto partner(UUID dossierId, int volgorde) {
        return dossierAdministrationService.findDetail(dossierId).partners().stream()
                .map(DetailPartnerDto::gegevens)
                .filter(p -> p.volgorde() == volgorde)
                .findFirst()
                .orElseThrow();
    }
}

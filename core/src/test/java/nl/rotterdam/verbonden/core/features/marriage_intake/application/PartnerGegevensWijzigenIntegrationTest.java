package nl.rotterdam.verbonden.core.features.marriage_intake.application;

import nl.rotterdam.verbonden.core.domain.BurgerServiceNummer;
import nl.rotterdam.verbonden.core.domain.Emailadres;
import nl.rotterdam.verbonden.core.domain.Telefoonnummer;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.PartnerGegevensDto;
import nl.rotterdam.verbonden.core.integration_test.CompleetDossierTestData;
import nl.rotterdam.verbonden.core.integration_test.VerbondenIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static nl.rotterdam.verbonden.core.integration_test.CompleetDossierTestData.BSN_PARTNER_1;
import static nl.rotterdam.verbonden.core.integration_test.CompleetDossierTestData.BSN_PARTNER_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@VerbondenIntegrationTest
@Transactional
class PartnerGegevensWijzigenIntegrationTest {

    @Autowired
    private MarriageIntakeService marriageIntakeService;

    @Autowired
    private CompleetDossierTestData testData;

    @Test
    void slaContactGegevensOp_metActueleVersie_slaatOpEnHoogtVersieOp() {
        UUID dossierId = testData.maakConceptDossier();
        long versie = partner(dossierId, BSN_PARTNER_1).versie();

        long nieuweVersie = marriageIntakeService.slaContactGegevensOp(dossierId, BSN_PARTNER_1, versie,
                new Telefoonnummer("0612345678"), new Emailadres("partner1@example.com"));

        PartnerGegevensDto partner = partner(dossierId, BSN_PARTNER_1);
        assertThat(nieuweVersie).isGreaterThan(versie);
        assertThat(partner.versie()).isEqualTo(nieuweVersie);
        assertThat(partner.telefoonnummer()).isEqualTo(new Telefoonnummer("0612345678"));
        assertThat(partner.emailadres()).isEqualTo(new Emailadres("partner1@example.com"));
    }

    @Test
    void slaContactGegevensOp_metVerouderdeVersie_wordtGeweigerd() {
        UUID dossierId = testData.maakConceptDossier();
        long versie = partner(dossierId, BSN_PARTNER_1).versie();
        marriageIntakeService.slaContactGegevensOp(dossierId, BSN_PARTNER_1, versie,
                new Telefoonnummer("0612345678"), null);

        assertThatThrownBy(() -> marriageIntakeService.slaContactGegevensOp(dossierId, BSN_PARTNER_1, versie,
                null, new Emailadres("partner1@example.com")))
                .isInstanceOf(OptimisticLockingFailureException.class);

        PartnerGegevensDto partner = partner(dossierId, BSN_PARTNER_1);
        assertThat(partner.telefoonnummer()).isEqualTo(new Telefoonnummer("0612345678"));
        assertThat(partner.emailadres()).isNull();
    }

    @Test
    void partnerKanContactGegevensEnAchternaamVanDeAnderVastleggen() {
        UUID dossierId = testData.maakConceptDossier();
        marriageIntakeService.acceptInvitation(dossierId, BSN_PARTNER_2);

        marriageIntakeService.slaPartnerGegevensOp(dossierId, BSN_PARTNER_2, "Jansen");
        marriageIntakeService.slaContactGegevensOp(dossierId, BSN_PARTNER_2, partner(dossierId, BSN_PARTNER_2).versie(),
                new Telefoonnummer("0687654321"), null);

        assertThat(partner(dossierId, BSN_PARTNER_2).gekozenAchternaam()).isEqualTo("Jansen");
        assertThat(partner(dossierId, BSN_PARTNER_2).telefoonnummer()).isEqualTo(new Telefoonnummer("0687654321"));
        assertThat(partner(dossierId, BSN_PARTNER_1).gekozenAchternaam()).isNull();
    }

    @Test
    void naamKeuze_hoogtVersieVanContactGegevensNietOp() {
        UUID dossierId = testData.maakConceptDossier();
        long versie = partner(dossierId, BSN_PARTNER_1).versie();

        marriageIntakeService.slaPartnerGegevensOp(dossierId, BSN_PARTNER_1, "Jansen");
        marriageIntakeService.slaContactGegevensOp(dossierId, BSN_PARTNER_1, versie,
                new Telefoonnummer("0612345678"), null);

        assertThat(partner(dossierId, BSN_PARTNER_1).telefoonnummer()).isEqualTo(new Telefoonnummer("0612345678"));
    }

    private PartnerGegevensDto partner(UUID dossierId, BurgerServiceNummer bsn) {
        return marriageIntakeService.findPartnerGegevens(dossierId).stream()
                .filter(p -> p.bsn().equals(bsn))
                .findFirst()
                .orElseThrow();
    }
}

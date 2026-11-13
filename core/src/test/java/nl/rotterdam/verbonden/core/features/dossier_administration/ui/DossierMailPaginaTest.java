package nl.rotterdam.verbonden.core.features.dossier_administration.ui;

import nl.rotterdam.verbonden.core.domain.Emailadres;
import nl.rotterdam.verbonden.core.features.dossier_administration.application.DossierAdministrationService;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.InzageActie;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.ListDossierMailDto;
import nl.rotterdam.verbonden.core.features.dossier_administration.repository.DossierInzageRepository;
import nl.rotterdam.verbonden.core.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.verbonden.core.identity.Roles;
import nl.rotterdam.verbonden.core.integration_test.BaseWicketTest;
import nl.rotterdam.verbonden.core.integration_test.CompleetDossierTestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@WithMockUser(username = "medewerker", roles = Roles.BEHEER)
class DossierMailPaginaTest extends BaseWicketTest {

    @Autowired
    private DossierAdministrationService dossierAdministrationService;

    @Autowired
    private MarriageIntakeService marriageIntakeService;

    @Autowired
    private DossierInzageRepository dossierInzageRepository;

    @Autowired
    private CompleetDossierTestData testData;

    @Test
    void detailPagina_toontDeVerzondenMailsMetLinkNaarDeInhoud() {
        UUID dossierId = maakIngediendDossier();

        tester.startPage(DossierDetailPage.class, DossierBeheerBasePage.parametersVoor(dossierId));

        tester.assertRenderedPage(DossierDetailPage.class);
        assertThat(tester.getLastResponseAsString())
                .contains("E-mails")
                .contains("Wij hebben uw aanvraag ontvangen")
                .contains("partner1@example.com", "partner2@example.com")
                .contains("In wacht")
                .contains("/mails/");
    }

    @Test
    void detailPagina_zonderMails_toontDatErNogGeenZijn() {
        UUID dossierId = testData.maakConceptDossier();

        tester.startPage(DossierDetailPage.class, DossierBeheerBasePage.parametersVoor(dossierId));

        assertThat(tester.getLastResponseAsString()).contains("Er zijn nog geen e-mails verstuurd.");
    }

    @Test
    void mailPagina_toontGegevensEnInhoudInEenBeveiligdeIframeEnLegtInzageVast() {
        UUID dossierId = maakIngediendDossier();
        ListDossierMailDto mail = dossierAdministrationService.findMails(dossierId).getFirst();

        tester.startPage(DossierMailDetailPage.class, DossierMailDetailPage.parametersVoor(dossierId, mail.id()));

        tester.assertRenderedPage(DossierMailDetailPage.class);
        assertThat(tester.getLastResponseAsString())
                .contains("Wij hebben uw aanvraag ontvangen")
                .contains(mail.ontvanger())
                .contains("Er is nog geen poging gedaan")
                .contains("sandbox=\"\"")
                // de HTML van de mail staat veilig als attribuutwaarde, niet als markup in de beheerpagina
                .contains("srcdoc=\"&lt;!DOCTYPE html&gt;")
                .contains("Kwik van Willegenburgh");
        assertThat(dossierInzageRepository.findByDossierUuidAndActie(dossierId, InzageActie.EMAIL_BEKEKEN))
                .hasSize(1);
    }

    @Test
    void mailPagina_metOnbekendeMail_gaatTerugNaarHetDossier() {
        UUID dossierId = maakIngediendDossier();

        tester.startPage(DossierMailDetailPage.class, DossierMailDetailPage.parametersVoor(dossierId, 987654321L));

        tester.assertRenderedPage(DossierDetailPage.class);
    }

    private UUID maakIngediendDossier() {
        UUID dossierId = testData.maakCompleetDossier();
        for (int volgorde : new int[]{1, 2}) {
            long versie = marriageIntakeService.findPartnerGegevens(dossierId).stream()
                    .filter(partner -> partner.volgorde() == volgorde)
                    .findFirst().orElseThrow().versie();
            marriageIntakeService.slaContactGegevensOp(dossierId, volgorde, versie, null,
                    new Emailadres("partner" + volgorde + "@example.com"));
        }
        marriageIntakeService.dienIn(dossierId);
        return dossierId;
    }
}

package nl.rotterdam.verbonden.core.features.dossier_mail.application;

import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.SendFailedException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import nl.rotterdam.verbonden.core.domain.Emailadres;
import nl.rotterdam.verbonden.core.features.dossier_administration.application.DossierAdministrationService;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.DossierMailDto;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.DossierMailPogingDto;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.InzageActie;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.ListDossierMailDto;
import nl.rotterdam.verbonden.core.features.dossier_administration.repository.DossierInzageRepository;
import nl.rotterdam.verbonden.core.features.dossier_mail.domain.DossierMailPogingStatus;
import nl.rotterdam.verbonden.core.features.dossier_mail.domain.DossierMailStatus;
import nl.rotterdam.verbonden.core.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.verbonden.core.identity.Roles;
import nl.rotterdam.verbonden.core.integration_test.CompleetDossierTestData;
import nl.rotterdam.verbonden.core.integration_test.InstelbareKlok;
import nl.rotterdam.verbonden.core.integration_test.OpvangendeMailSender;
import nl.rotterdam.verbonden.core.integration_test.VerbondenIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSendException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@VerbondenIntegrationTest
@Transactional
@WithMockUser(username = "medewerker", roles = Roles.BEHEER)
class DossierMailIntegrationTest {

    private static final LocalDate DAG = LocalDate.of(2026, 10, 26);
    private static final LocalDateTime NU = DAG.atTime(12, 0);

    @Autowired
    private DossierMailService dossierMailService;

    @Autowired
    private DossierAdministrationService dossierAdministrationService;

    @Autowired
    private MarriageIntakeService marriageIntakeService;

    @Autowired
    private DossierInzageRepository dossierInzageRepository;

    @Autowired
    private CompleetDossierTestData testData;

    @Autowired
    private InstelbareKlok klok;

    @Autowired
    private OpvangendeMailSender mailSender;

    @BeforeEach
    void zetKlokVast() {
        klok.zetOp(DAG);
        mailSender.reset();
    }

    @AfterEach
    void zetKlokTerug() {
        klok.zetTerug();
    }

    @Test
    void dienIn_legtBevestigingsmailVastVoorBeidePartners() {
        UUID dossierId = maakIngediendDossier(true);

        List<ListDossierMailDto> mails = dossierAdministrationService.findMails(dossierId);

        assertThat(mails).hasSize(1);
        ListDossierMailDto mail = mails.getFirst();
        assertThat(mail.onderwerp()).isEqualTo("Wij hebben uw aanvraag ontvangen");
        assertThat(mail.ontvangers()).containsExactly("partner1@example.com", "partner2@example.com");
        assertThat(mail.status()).isEqualTo(DossierMailStatus.IN_WACHT);
        assertThat(mail.aantalPogingen()).isZero();
        assertThat(mail.aangemaaktOp()).isEqualTo(NU);

        DossierMailDto detail = dossierAdministrationService.findMail(dossierId, mail.id());
        assertThat(detail.afzender()).isEqualTo("Gemeente Rotterdam <noreply@rotterdam.nl>");
        assertThat(detail.inhoud())
                .contains("Kwik van Willegenburgh", "Kwek van Willegenburgh", "Klein", "Wat gebeurt er nu?")
                .contains("<li>" + naamMetKeuze(dossierId, 1) + "</li>", "<li>" + naamMetKeuze(dossierId, 2) + "</li>")
                .doesNotContain("Jansen - Jansen")
                .doesNotContain("{{");
        assertThat(mailSender.berichten()).isEmpty();
    }

    @Test
    void verwerkOpenstaandeMails_verstuurtDeMailNaarBeidePartnersEnLegtDePogingVast() throws Exception {
        UUID dossierId = maakIngediendDossier(true);

        assertThat(dossierMailService.verwerkOpenstaandeMails()).isEqualTo(1);

        assertThat(mailSender.berichten()).hasSize(1);
        MimeMessage bericht = mailSender.berichten().getFirst();
        assertThat(bericht.getRecipients(Message.RecipientType.TO))
                .extracting(adres -> ((InternetAddress) adres).getAddress())
                .containsExactly("partner1@example.com", "partner2@example.com");
        assertThat(bericht.getFrom()[0].toString()).isEqualTo("Gemeente Rotterdam <noreply@rotterdam.nl>");
        assertThat(bericht.getSubject()).isEqualTo("Wij hebben uw aanvraag ontvangen");
        bericht.saveChanges();
        assertThat(bericht.getContentType()).contains("text/html");

        ListDossierMailDto mail = dossierAdministrationService.findMails(dossierId).getFirst();
        assertThat(mail.status()).isEqualTo(DossierMailStatus.VERZONDEN);
        assertThat(mail.volgendePogingOp()).isNull();
        assertThat(pogingen(dossierId, mail.id()))
                .extracting(DossierMailPogingDto::status)
                .containsExactly(DossierMailPogingStatus.GESLAAGD);

        assertThat(dossierMailService.verwerkOpenstaandeMails()).isZero();
        assertThat(mailSender.berichten()).hasSize(1);
    }

    @Test
    void verwerkOpenstaandeMails_bijStoring_probeertOpnieuwMetBackOffEnGeeftOpNaMaxPogingen() {
        UUID dossierId = maakIngediendDossier(true);
        mailSender.faalAltijdMet(new MailSendException("Mailserver niet bereikbaar"));
        long mailId = dossierAdministrationService.findMails(dossierId).getFirst().id();

        assertThat(dossierMailService.verwerkOpenstaandeMails()).isEqualTo(1);
        ListDossierMailDto na1 = dossierAdministrationService.findMails(dossierId).getFirst();
        assertThat(na1.status()).isEqualTo(DossierMailStatus.IN_WACHT);
        assertThat(na1.volgendePogingOp()).isEqualTo(NU.plusMinutes(1));

        // Nog niet aan de beurt
        assertThat(dossierMailService.verwerkOpenstaandeMails()).isZero();

        // De wachttijd verdubbelt na elke poging: 1, 2, 4 en 8 minuten
        for (int dag = 1; dag <= 3; dag++) {
            klok.zetOp(DAG.plusDays(dag));
            assertThat(dossierMailService.verwerkOpenstaandeMails()).isEqualTo(1);
            assertThat(dossierAdministrationService.findMails(dossierId).getFirst().volgendePogingOp())
                    .isEqualTo(NU.plusDays(dag).plusMinutes(1L << dag));
        }

        klok.zetOp(DAG.plusDays(4));
        assertThat(dossierMailService.verwerkOpenstaandeMails()).isEqualTo(1);
        ListDossierMailDto laatste = dossierAdministrationService.findMails(dossierId).getFirst();
        assertThat(laatste.status()).isEqualTo(DossierMailStatus.MISLUKT);
        assertThat(laatste.volgendePogingOp()).isNull();
        assertThat(laatste.aantalPogingen()).isEqualTo(5);

        klok.zetOp(DAG.plusDays(5));
        assertThat(dossierMailService.verwerkOpenstaandeMails()).isZero();

        List<DossierMailPogingDto> pogingen = pogingen(dossierId, mailId);
        assertThat(pogingen).extracting(DossierMailPogingDto::volgnummer).containsExactly(1, 2, 3, 4, 5);
        assertThat(pogingen).extracting(DossierMailPogingDto::status)
                .containsOnly(DossierMailPogingStatus.MISLUKT);
        assertThat(pogingen.getFirst().foutmelding()).contains("Mailserver niet bereikbaar");
    }

    @Test
    void verwerkOpenstaandeMails_alsDeMailserverHerstelt_wordtDeMailAlsnogVerzonden() {
        UUID dossierId = maakIngediendDossier(true);
        mailSender.faalEenKeerMet(new MailSendException("Mailserver niet bereikbaar"));
        long mailId = dossierAdministrationService.findMails(dossierId).getFirst().id();

        dossierMailService.verwerkOpenstaandeMails();
        klok.zetOp(DAG.plusDays(1));
        dossierMailService.verwerkOpenstaandeMails();

        assertThat(dossierAdministrationService.findMails(dossierId).getFirst().status())
                .isEqualTo(DossierMailStatus.VERZONDEN);
        assertThat(pogingen(dossierId, mailId)).extracting(DossierMailPogingDto::status)
                .containsExactly(DossierMailPogingStatus.MISLUKT, DossierMailPogingStatus.GESLAAGD);
        assertThat(mailSender.berichten()).hasSize(1);
    }

    @Test
    void verwerkOpenstaandeMails_alsAdresWordtAfgewezen_wordtNietOpnieuwGeprobeerd() throws Exception {
        UUID dossierId = maakIngediendDossier(true);
        SendFailedException afwijzing = new SendFailedException("Ongeldig adres", null, new Address[0], new Address[0],
                new Address[]{new InternetAddress("partner1@example.com")});
        mailSender.faalAltijdMet(new MailSendException("Verzenden mislukt", afwijzing));

        dossierMailService.verwerkOpenstaandeMails();

        ListDossierMailDto mail = dossierAdministrationService.findMails(dossierId).getFirst();
        assertThat(mail.status()).isEqualTo(DossierMailStatus.MISLUKT);
        assertThat(mail.aantalPogingen()).isEqualTo(1);
        assertThat(mail.volgendePogingOp()).isNull();
    }

    @Test
    void dienIn_zonderEmailadressen_legtMislukteMailVast() {
        UUID dossierId = maakIngediendDossier(false);

        ListDossierMailDto mail = dossierAdministrationService.findMails(dossierId).getFirst();

        assertThat(mail.status()).isEqualTo(DossierMailStatus.MISLUKT);
        assertThat(mail.ontvangers()).isEmpty();
        assertThat(mail.aantalPogingen()).isEqualTo(1);
        assertThat(dossierMailService.verwerkOpenstaandeMails()).isZero();
        assertThat(mailSender.berichten()).isEmpty();
    }

    @Test
    void findMail_legtInzageVast_enWeigertEenMailVanEenOnbekendDossier() {
        UUID dossierId = maakIngediendDossier(true);
        long mailId = dossierAdministrationService.findMails(dossierId).getFirst().id();

        dossierAdministrationService.findMail(dossierId, mailId);

        assertThat(dossierInzageRepository.findByDossierUuidAndActie(dossierId, InzageActie.EMAIL_BEKEKEN))
                .hasSize(1);
        assertThatThrownBy(() -> dossierAdministrationService.findMail(UUID.randomUUID(), mailId))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private String naamMetKeuze(UUID dossierId, int volgorde) {
        var partner = marriageIntakeService.findPartnerGegevens(dossierId).stream()
                .filter(p -> p.volgorde() == volgorde).findFirst().orElseThrow();
        return partner.voornamen() + " " + partner.gekozenAchternaam();
    }

    private List<DossierMailPogingDto> pogingen(UUID dossierId, long mailId) {
        return dossierAdministrationService.findMail(dossierId, mailId).pogingen();
    }

    private UUID maakIngediendDossier(boolean metEmailadressen) {
        UUID dossierId = testData.maakCompleetDossier();
        if (metEmailadressen) {
            zetEmailadres(dossierId, 1, "partner1@example.com");
            zetEmailadres(dossierId, 2, "partner2@example.com");
        }
        marriageIntakeService.dienIn(dossierId);
        return dossierId;
    }

    private void zetEmailadres(UUID dossierId, int volgorde, String emailadres) {
        long versie = marriageIntakeService.findPartnerGegevens(dossierId).stream()
                .filter(partner -> partner.volgorde() == volgorde)
                .findFirst().orElseThrow().versie();
        marriageIntakeService.slaContactGegevensOp(dossierId, volgorde, versie, null, new Emailadres(emailadres));
    }
}

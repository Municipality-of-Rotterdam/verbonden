package nl.rotterdam.verbonden.core.features.marriage_intake.ui;

import nl.rotterdam.verbonden.core.domain.Bedrag;
import nl.rotterdam.verbonden.core.features.dossier_administration.application.DossierAdministrationService;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.CreateBetaalmutatieDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.verbonden.core.features.marriage_intake.application.OnlineBetalenService;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierStatus;
import nl.rotterdam.verbonden.core.integration_test.BaseWicketTest;
import nl.rotterdam.verbonden.core.integration_test.CompleetDossierTestData;
import nl.rotterdam.verbonden.core.integration_test.TestOnlineBetaalService;
import nl.rotterdam.verbonden.core.online_betalen.BetaalUitslag;
import nl.rotterdam.verbonden.core.online_betalen.BetaalUitslagMelding;
import nl.rotterdam.verbonden.core.online_betalen.BetaalUitslagVerwerker;
import nl.rotterdam.verbonden.core.online_betalen.StartBetalingOpdracht;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static nl.rotterdam.verbonden.core.features.marriage_intake.ui.DossierPageParameterUtil.makeDossierPageParameters;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * De hele betaalflow: van de knop "Betalen" via de betaalprovider terug naar Verbonden.
 */
@Transactional
class BetalingRetourPageTest extends BaseWicketTest {

    private static final String PAGE_BODY = "pageLayout:pageLayout_body:pageBody:pageBody_body";
    private static final String BEVESTIG_FORM = PAGE_BODY + ":bevestigForm";

    @Autowired
    private CompleetDossierTestData testData;

    @Autowired
    private MarriageIntakeService marriageIntakeService;

    @Autowired
    private BetaalUitslagVerwerker verwerker;

    @Autowired
    private TestOnlineBetaalService betaalprovider;

    @Autowired
    private DossierAdministrationService dossierAdministrationService;

    @Autowired
    private OnlineBetalenService onlineBetalenService;

    @AfterEach
    void resetBetaalprovider() {
        betaalprovider.reset();
    }

    @Test
    @WithMockUser(username = "999990020")
    void betalenStuurtDoorNaarBetaalproviderMetRetourUrl() {
        UUID dossierId = testData.maakCompleetBetaaldDossier();

        klikBetalen(dossierId);

        StartBetalingOpdracht opdracht = betaalprovider.laatsteOpdracht();
        assertThat(tester.getLastResponse().getRedirectLocation())
                .isEqualTo(TestOnlineBetaalService.BETAALPAGINA + opdracht.betalingskenmerk());
        assertThat(opdracht.retourUrl())
                .startsWith("http://")
                .endsWith("/mijn-dag/" + dossierId + "/betaling-retour");
        assertThat(marriageIntakeService.findStatus(dossierId)).isEqualTo(DossierStatus.CONCEPT);
    }

    @Test
    @WithMockUser(username = "999990020")
    void terugZonderUitslag_wachtOpDeUitslagEnGaatDaarnaDoor() {
        UUID dossierId = testData.maakCompleetBetaaldDossier();
        klikBetalen(dossierId);

        tester.startPage(BetalingRetourPage.class, makeDossierPageParameters(dossierId));

        tester.assertRenderedPage(BetalingRetourPage.class);
        tester.assertContains("Uw betaling is in behandeling");
        assertPaginaSerialiseerbaar();

        verwerker.verwerkUitslag(new BetaalUitslagMelding(betaalprovider.laatsteOpdracht().betalingskenmerk(), null, null, BetaalUitslag.GELUKT));
        AbstractAjaxTimerBehavior timer = tester.getLastRenderedPage().getBehaviors(AbstractAjaxTimerBehavior.class)
                .getFirst();
        tester.executeBehavior(timer);
        tester.assertRenderedPage(AanvraagStatusPage.class);
    }

    @Test
    @WithMockUser(username = "999990020")
    void terugNaGeluktBetaling_toontStatuspagina() {
        UUID dossierId = testData.maakCompleetBetaaldDossier();
        klikBetalen(dossierId);
        verwerker.verwerkUitslag(new BetaalUitslagMelding(betaalprovider.laatsteOpdracht().betalingskenmerk(), null, null, BetaalUitslag.GELUKT));

        tester.startPage(BetalingRetourPage.class, makeDossierPageParameters(dossierId));

        tester.assertRenderedPage(AanvraagStatusPage.class);
        tester.assertContains("We hebben uw aanvraag ontvangen.");
    }

    @Test
    @WithMockUser(username = "999990020")
    void terugNaMislukteBetaling_toontControlerenMetEenmaligeMelding() {
        UUID dossierId = testData.maakCompleetBetaaldDossier();
        klikBetalen(dossierId);
        verwerker.verwerkUitslag(new BetaalUitslagMelding(betaalprovider.laatsteOpdracht().betalingskenmerk(), null, null, BetaalUitslag.MISLUKT));

        tester.startPage(BetalingRetourPage.class, makeDossierPageParameters(dossierId));

        tester.assertRenderedPage(ControlerenPage.class);
        tester.assertVisible(PAGE_BODY + ":betalingMislukt");
        tester.assertContains("De betaling is niet gelukt of afgebroken.");
        assertThat(marriageIntakeService.findStatus(dossierId)).isEqualTo(DossierStatus.CONCEPT);

        // De melding is eenmalig: na een refresh van de pagina is hij weg
        tester.startPage(tester.getLastRenderedPage());
        tester.assertRenderedPage(ControlerenPage.class);
        tester.assertInvisible(PAGE_BODY + ":betalingMislukt");
    }

    @Test
    @WithMockUser(username = "999990020")
    void retourParameterBetaaldJa_wordtGenegeerd() {
        UUID dossierId = testData.maakCompleetBetaaldDossier();
        klikBetalen(dossierId);

        PageParameters parameters = makeDossierPageParameters(dossierId).set("betaald", "JA");
        tester.startPage(BetalingRetourPage.class, parameters);

        tester.assertRenderedPage(BetalingRetourPage.class);
        assertThat(marriageIntakeService.findStatus(dossierId)).isEqualTo(DossierStatus.CONCEPT);
    }

    @Test
    @WithMockUser(username = "999990020")
    void betaalproviderNietBereikbaar_toontControlerenMetMelding() {
        UUID dossierId = testData.maakCompleetBetaaldDossier();
        betaalprovider.zetNietBeschikbaar();

        klikBetalen(dossierId);

        tester.assertRenderedPage(ControlerenPage.class);
        tester.assertContains("De betaling is niet gelukt of afgebroken.");
    }

    @Test
    @WithMockUser(username = "999990020")
    void retourZonderBetaling_gaatNaarControleren() {
        UUID dossierId = testData.maakCompleetBetaaldDossier();

        tester.startPage(BetalingRetourPage.class, makeDossierPageParameters(dossierId));

        tester.assertRenderedPage(ControlerenPage.class);
        tester.assertInvisible(PAGE_BODY + ":betalingMislukt");
    }

    @Test
    @WithMockUser(username = "999990020")
    void langzameBank_naTweeMinutenUitlegDatDeBurgerNietHoeftTeWachten() {
        UUID dossierId = testData.maakCompleetBetaaldDossier();
        klikBetalen(dossierId);
        tester.startPage(BetalingRetourPage.class, makeDossierPageParameters(dossierId));
        AbstractAjaxTimerBehavior timer = tester.getLastRenderedPage().getBehaviors(AbstractAjaxTimerBehavior.class)
                .getFirst();

        for (int ronde = 1; ronde < BetalingRetourPage.LANGZAAM_NA_RONDES; ronde++) {
            tester.executeBehavior(timer);
        }
        tester.assertInvisible(PAGE_BODY + ":langzaam");

        assertThat(timer.isStopped()).isFalse();

        tester.executeBehavior(timer);
        tester.assertVisible(PAGE_BODY + ":langzaam");
        tester.assertContains("U kunt deze pagina sluiten.");
        tester.assertRenderedPage(BetalingRetourPage.class);
        // Niet eindeloos blijven kijken: dat houdt de sessie levend
        assertThat(timer.isStopped()).isTrue();
    }

    @Test
    @WithMockUser(username = "999990020")
    void betaalmutatie_staatOpDeStatuspaginaEnIsTeBetalen() {
        UUID dossierId = ingediendDossier();
        dossierAdministrationService.createBetaalmutatie(dossierId,
                new CreateBetaalmutatieDto("Trouwboekje Luxe (bijbesteld)", Bedrag.of("25.00")));

        tester.startPage(AanvraagStatusPage.class, makeDossierPageParameters(dossierId));
        tester.assertVisible(PAGE_BODY + ":teBetalen");
        assertPaginaSerialiseerbaar();
        tester.assertContains("Trouwboekje Luxe \\(bijbesteld\\)");
        tester.assertContains("25,00");

        tester.getRequest().setHeader("Origin", "http://localhost");
        formTester("betalenForm").submit("betalen");

        StartBetalingOpdracht opdracht = betaalprovider.laatsteOpdracht();
        assertThat(opdracht.bedrag()).isEqualTo(Bedrag.of("25.00"));
        assertThat(tester.getLastResponse().getRedirectLocation())
                .isEqualTo(TestOnlineBetaalService.BETAALPAGINA + opdracht.betalingskenmerk());

        // Terug van de betaalprovider: eerst wachten, na de uitslag de statuspagina zonder "Te betalen"
        tester.startPage(BetalingRetourPage.class, makeDossierPageParameters(dossierId));
        tester.assertRenderedPage(BetalingRetourPage.class);
        verwerker.verwerkUitslag(new BetaalUitslagMelding(opdracht.betalingskenmerk(), null, null, BetaalUitslag.GELUKT));
        tester.startPage(BetalingRetourPage.class, makeDossierPageParameters(dossierId));
        tester.assertRenderedPage(AanvraagStatusPage.class);
        tester.assertInvisible(PAGE_BODY + ":teBetalen");
    }

    @Test
    @WithMockUser(username = "999990020")
    void mislukteBetalingVanEenMutatie_terugNaarStatuspaginaMetMelding() {
        UUID dossierId = ingediendDossier();
        long mutatieId = dossierAdministrationService.createBetaalmutatie(dossierId,
                new CreateBetaalmutatieDto("Internationale akte", Bedrag.of("17.80")));
        onlineBetalenService.startMutatieBetaling(dossierId, mutatieId,
                CompleetDossierTestData.BSN_PARTNER_1, "https://verbonden.test/retour");
        verwerker.verwerkUitslag(new BetaalUitslagMelding(betaalprovider.laatsteOpdracht().betalingskenmerk(), null, null, BetaalUitslag.MISLUKT));

        tester.startPage(BetalingRetourPage.class, makeDossierPageParameters(dossierId));

        tester.assertRenderedPage(AanvraagStatusPage.class);
        tester.assertVisible(PAGE_BODY + ":betalingMislukt");
        tester.assertVisible(PAGE_BODY + ":teBetalen");
    }

    @Test
    @WithMockUser(username = "999990020")
    void betaalproviderNietBereikbaarBijEenMutatie_statuspaginaMetMelding() {
        UUID dossierId = ingediendDossier();
        dossierAdministrationService.createBetaalmutatie(dossierId,
                new CreateBetaalmutatieDto("Internationale akte", Bedrag.of("17.80")));
        betaalprovider.zetNietBeschikbaar();

        tester.startPage(AanvraagStatusPage.class, makeDossierPageParameters(dossierId));
        tester.getRequest().setHeader("Origin", "http://localhost");
        formTester("betalenForm").submit("betalen");

        tester.assertRenderedPage(AanvraagStatusPage.class);
        tester.assertContains("De betaling is niet gelukt of afgebroken.");
    }

    @Test
    @WithMockUser(username = "999990020")
    void betalingInBehandeling_nogmaalsBevestigenGaatNaarDeWachtpagina() {
        UUID dossierId = testData.maakCompleetBetaaldDossier();
        klikBetalen(dossierId);

        klikBetalen(dossierId);

        tester.assertRenderedPage(BetalingRetourPage.class);
        assertThat(betaalprovider.opdrachten()).hasSize(1);
    }

    @Test
    @WithMockUser(username = "999990020")
    void statuspagina_stapBetalingToontBetaaldEnOpenstaand() {
        UUID dossierId = ingediendDossier();

        tester.startPage(AanvraagStatusPage.class, makeDossierPageParameters(dossierId));
        tester.assertContains("Betaling gelukt");
        tester.assertContains("U heeft € 17,80 betaald.");
        assertThat(tester.getLastResponseAsString()).doesNotContain("factuur");

        long mutatieId = dossierAdministrationService.createBetaalmutatie(dossierId,
                new CreateBetaalmutatieDto("Trouwboekje Luxe (bijbesteld)", Bedrag.of("25.00")));
        tester.startPage(AanvraagStatusPage.class, makeDossierPageParameters(dossierId));
        tester.assertContains("Nog te betalen");
        tester.assertContains("U heeft € 17,80 betaald. Er staat nog € 25,00 open.");

        onlineBetalenService.startMutatieBetaling(dossierId, mutatieId,
                CompleetDossierTestData.BSN_PARTNER_1, "https://verbonden.test/retour");
        tester.startPage(AanvraagStatusPage.class, makeDossierPageParameters(dossierId));
        tester.assertContains("Betaling in behandeling");
        tester.assertContains("Uw bank moet de betaling van € 25,00 nog bevestigen.");
        // Geen tweede betaling starten terwijl de eerste nog loopt
        tester.assertInvisible(pad("betalenForm"));
        tester.assertVisible(pad("inBehandeling"));

        String kenmerk = betaalprovider.laatsteOpdracht().betalingskenmerk();
        verwerker.verwerkUitslag(new BetaalUitslagMelding(kenmerk, null, null, BetaalUitslag.MISLUKT));
        tester.startPage(AanvraagStatusPage.class, makeDossierPageParameters(dossierId));
        tester.assertContains("Betaling niet gelukt");
        tester.assertContains("De betaling van € 25,00 is niet gelukt.");
        tester.assertVisible(pad("betalenForm"));

        onlineBetalenService.startMutatieBetaling(dossierId, mutatieId,
                CompleetDossierTestData.BSN_PARTNER_1, "https://verbonden.test/retour");
        verwerker.verwerkUitslag(new BetaalUitslagMelding(betaalprovider.laatsteOpdracht().betalingskenmerk(), null, null, BetaalUitslag.GELUKT));
        tester.startPage(AanvraagStatusPage.class, makeDossierPageParameters(dossierId));
        tester.assertContains("Betaling gelukt");
        tester.assertContains("U heeft € 42,80 betaald.");
        tester.assertInvisible(PAGE_BODY + ":teBetalen");
    }

    @Test
    @WithMockUser(username = "999990020")
    void statuspagina_zonderBetalingGeenStapBetaling() {
        UUID dossierId = testData.maakCompleetDossier();
        marriageIntakeService.dienIn(dossierId);

        tester.startPage(AanvraagStatusPage.class, makeDossierPageParameters(dossierId));

        tester.assertInvisible(PAGE_BODY + ":vervolg:stapBetaling");
    }

    @Test
    @WithMockUser(username = "999990020")
    void intussenVerwijderdeMutatie_betalenToontDeActueleStand() {
        UUID dossierId = ingediendDossier();
        long mutatieId = dossierAdministrationService.createBetaalmutatie(dossierId,
                new CreateBetaalmutatieDto("Vergissing", Bedrag.of("10.00")));
        tester.startPage(AanvraagStatusPage.class, makeDossierPageParameters(dossierId));
        dossierAdministrationService.deleteBetaalmutatie(dossierId, mutatieId);

        tester.getRequest().setHeader("Origin", "http://localhost");
        formTester("betalenForm").submit("betalen");

        tester.assertRenderedPage(AanvraagStatusPage.class);
        tester.assertInvisible(PAGE_BODY + ":teBetalen");
    }

    /** Een ingediend dossier, betaald via de normale flow. */
    private UUID ingediendDossier() {
        UUID dossierId = testData.maakCompleetBetaaldDossier();
        klikBetalen(dossierId);
        verwerker.verwerkUitslag(new BetaalUitslagMelding(betaalprovider.laatsteOpdracht().betalingskenmerk(), null, null, BetaalUitslag.GELUKT));
        return dossierId;
    }

    /** "Bevestigen en betalen" op de controleerpagina. */
    private void klikBetalen(UUID dossierId) {
        tester.startPage(ControlerenPage.class, makeDossierPageParameters(dossierId));
        // Zoals een browser: anders weigert Wicket's CSRF-bescherming de POST.
        tester.getRequest().setHeader("Origin", "http://localhost");
        tester.newFormTester(BEVESTIG_FORM).submit("bevestigButton");
    }
}

package nl.rotterdam.verbonden.core.features.betaling_administration.ui;

import nl.rotterdam.verbonden.core.domain.Bedrag;
import nl.rotterdam.verbonden.core.features.dossier_administration.application.DossierAdministrationService;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.CreateBetaalmutatieDto;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.ListBetaalmutatieDto;
import nl.rotterdam.verbonden.core.features.dossier_administration.ui.BetaalmutatieCreatePage;
import nl.rotterdam.verbonden.core.features.dossier_administration.ui.DossierDetailPage;
import nl.rotterdam.verbonden.core.features.marriage_intake.application.OnlineBetalenService;
import nl.rotterdam.verbonden.core.identity.Roles;
import nl.rotterdam.verbonden.core.integration_test.BaseWicketTest;
import nl.rotterdam.verbonden.core.integration_test.CompleetDossierTestData;
import nl.rotterdam.verbonden.core.integration_test.TestOnlineBetaalService;
import nl.rotterdam.verbonden.core.online_betalen.BetaalUitslag;
import nl.rotterdam.verbonden.core.online_betalen.BetaalUitslagMelding;
import nl.rotterdam.verbonden.core.online_betalen.BetaalUitslagVerwerker;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.tester.FormTester;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static nl.rotterdam.verbonden.core.integration_test.CompleetDossierTestData.BSN_PARTNER_1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@Transactional
@WithMockUser(username = "medewerker", roles = Roles.MEDEWERKER)
class BetalingenBeheerPaginaTest extends BaseWicketTest {

    @Autowired
    private CompleetDossierTestData testData;

    @Autowired
    private OnlineBetalenService onlineBetalenService;

    @Autowired
    private DossierAdministrationService dossierAdministrationService;

    @Autowired
    private BetaalUitslagVerwerker verwerker;

    @Autowired
    private TestOnlineBetaalService betaalprovider;

    @AfterEach
    void resetBetaalprovider() {
        betaalprovider.reset();
    }

    @Test
    void overzicht_toontBetalingenEnZoektOpKenmerk() {
        UUID dossierId = ingediendDossier();
        String kenmerk = betaalprovider.laatsteOpdracht().betalingskenmerk();

        tester.startPage(BetalingAdministrationPage.class);

        tester.assertRenderedPage(BetalingAdministrationPage.class);
        assertThat(tester.getLastResponseAsString())
                .contains(kenmerk)
                .contains(dossierId.toString())
                .contains("Gelukt")
                .contains("Aanvraag");
        assertPaginaSerialiseerbaar();

        FormTester zoeken = formTester("zoekForm");
        zoeken.setValue("zoektermVeld", "geen-enkel-kenmerk");
        tester.executeAjaxEvent(pad("zoekButton"), "click");
        assertThat(tester.getLastResponseAsString()).doesNotContain(kenmerk);
    }

    /**
     * Wat je bij OBM of de betaalprovider ziet (betalingskenmerk, basket-id), vindt de betaling met een link naar
     * het dossier terug.
     */
    @Test
    void zoekenOpKenmerkUitObmVindtHetDossier() {
        UUID dossierId = ingediendDossier();
        String kenmerk = betaalprovider.laatsteOpdracht().betalingskenmerk();
        tester.startPage(BetalingAdministrationPage.class);

        for (String zoekterm : List.of(kenmerk, "extern-" + kenmerk)) {
            FormTester zoeken = formTester("zoekForm");
            zoeken.setValue("zoektermVeld", zoekterm);
            tester.executeAjaxEvent(pad("zoekButton"), "click");
            assertThat(tester.getLastResponseAsString()).as(zoekterm)
                    .contains(kenmerk)
                    .contains(dossierId.toString());
        }

        FormTester zoeken = formTester("zoekForm");
        zoeken.setValue("zoektermVeld", "extern-");
        tester.executeAjaxEvent(pad("zoekButton"), "click");
        // Het kenmerk bij de betaalprovider moet precies kloppen
        assertThat(tester.getLastResponseAsString()).doesNotContain(kenmerk);
    }

    /** Een leeg tekstveld geeft in Wicket {@code null}. */
    @Test
    void zoekenMetLeegZoekveld_toontAlles() {
        ingediendDossier();
        String kenmerk = betaalprovider.laatsteOpdracht().betalingskenmerk();
        tester.startPage(BetalingAdministrationPage.class);

        FormTester zoeken = formTester("zoekForm");
        zoeken.setValue("zoektermVeld", "");
        tester.executeAjaxEvent(pad("zoekButton"), "click");

        assertThat(tester.getLastResponseAsString()).contains(kenmerk);
    }

    @Test
    void detailpagina_toontBetalingenEnBetaalmutaties() {
        UUID dossierId = ingediendDossier();
        dossierAdministrationService.createBetaalmutatie(dossierId,
                new CreateBetaalmutatieDto("Trouwboekje Luxe (bijbesteld)", Bedrag.of("25.00")));

        tester.startPage(DossierDetailPage.class, detailParameters(dossierId));

        assertThat(tester.getLastResponseAsString())
                .contains(betaalprovider.laatsteOpdracht().betalingskenmerk())
                .contains("Gelukt")
                .contains("Trouwboekje Luxe (bijbesteld)")
                .contains("Te betalen");
        tester.assertVisible(pad("betaalmutatieToevoegen"));
        assertPaginaSerialiseerbaar();
    }

    @Test
    void conceptDossier_geenBetaalmutatieToevoegen() {
        UUID dossierId = testData.maakCompleetBetaaldDossier();

        tester.startPage(DossierDetailPage.class, detailParameters(dossierId));

        tester.assertInvisible(pad("betaalmutatieToevoegen"));
        tester.assertContains("Er zijn nog geen online betalingen.");
    }

    @Test
    void betaalmutatieToevoegen_komtOpDeDetailpagina() {
        UUID dossierId = ingediendDossier();

        tester.startPage(BetaalmutatieCreatePage.class, detailParameters(dossierId));
        assertPaginaSerialiseerbaar();
        FormTester form = formTester("betaalmutatieForm");
        form.setValue(veld("betaalmutatieForm", "omschrijving"), "Internationale akte");
        form.setValue(veld("betaalmutatieForm", "bedrag"), "17.80");
        tester.addRequestHeader("sec-fetch-site", "same-origin");
        form.submit(veld("betaalmutatieForm", "toevoegen"));

        tester.assertRenderedPage(DossierDetailPage.class);
        assertThat(dossierAdministrationService.findDetail(dossierId).betaalmutaties())
                .extracting(ListBetaalmutatieDto::omschrijving, ListBetaalmutatieDto::bedrag)
                .containsExactly(tuple("Internationale akte", Bedrag.of("17.80")));
        assertThat(onlineBetalenService.findTeBetalen(dossierId)).hasSize(1);
    }

    @Test
    void betaalmutatieZonderBedrag_wordtNietToegevoegd() {
        UUID dossierId = ingediendDossier();

        tester.startPage(BetaalmutatieCreatePage.class, detailParameters(dossierId));
        FormTester form = formTester("betaalmutatieForm");
        form.setValue(veld("betaalmutatieForm", "omschrijving"), "Internationale akte");
        tester.addRequestHeader("sec-fetch-site", "same-origin");
        form.submit(veld("betaalmutatieForm", "toevoegen"));

        tester.assertRenderedPage(BetaalmutatieCreatePage.class);
        assertThat(dossierAdministrationService.findDetail(dossierId).betaalmutaties()).isEmpty();
    }

    private UUID ingediendDossier() {
        UUID dossierId = testData.maakCompleetBetaaldDossier();
        onlineBetalenService.startBetaling(dossierId, BSN_PARTNER_1, "https://verbonden.test/retour");
        verwerker.verwerkUitslag(new BetaalUitslagMelding(betaalprovider.laatsteOpdracht().betalingskenmerk(), null, null, BetaalUitslag.GELUKT));
        return dossierId;
    }

    private static PageParameters detailParameters(UUID dossierId) {
        return new PageParameters().set("dossierId", dossierId.toString());
    }
}

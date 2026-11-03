package nl.rotterdam.verbonden.core.features.marriage_intake.ui;

import nl.rotterdam.verbonden.core.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.verbonden.core.features.marriage_intake.application.OnlineBetalenService;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.BetalingStandDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.BetalingStatus;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierSamenvattingDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierStatus;
import nl.rotterdam.verbonden.core.online_betalen.BetaalSoort;
import nl.rotterdam.nl_design_system.wicket.components.heading.RdHeading;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.time.Duration;
import java.util.Optional;

import static nl.rotterdam.verbonden.core.features.marriage_intake.ui.DossierPageParameterUtil.makeDossierPageParameters;

/**
 * Hier komt de burger terug van de betaalprovider, na de betaling van de aanvraag of van een betaalmutatie. Wat de
 * betaalprovider in de URL meestuurt, wordt bewust genegeerd: dat is niet te vertrouwen. De pagina kijkt naar de
 * uitslag die de betaalprovider los van de browser heeft gemeld. Is die er nog niet, dan wacht de pagina erop.
 */
public class BetalingRetourPage extends IntakeBasePage {

    static final Duration WACHTTIJD = Duration.ofSeconds(3);

    /**
     * Na zoveel keer kijken (10 minuten) krijgt de burger uitleg dat de bank er langer over doet dan normaal. Vijf
     * minuten is gewoon.
     */
    static final int LANGZAAM_NA_RONDES = 200;

    @SpringBean
    private MarriageIntakeService marriageIntakeService;

    @SpringBean
    private OnlineBetalenService onlineBetalenService;

    private final IModel<DossierSamenvattingDto> dossierModel =
            LoadableDetachableModel.of(() -> marriageIntakeService.findByDossierId(dossierId));

    private Label langzaam;

    @Override
    protected IntakeStep getActiveStep() {
        return IntakeStep.CONTROLEREN;
    }

    @Override
    protected boolean hasStepNavigation() {
        return false;
    }

    @Override
    protected boolean isToegestaanNaIndienen() {
        // Betaalmutaties worden betaald nadat het dossier is ingediend
        return true;
    }

    @Override
    protected IModel<String> getTitleModel() {
        return new ResourceModel("betaling.retour.page.title");
    }

    @Override
    protected IModel<DossierSamenvattingDto> getSidebarDossierModel() {
        return dossierModel;
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        Optional<BetalingStandDto> stand = onlineBetalenService.findLaatsteBetaling(dossierId);
        if (stand.isEmpty()) {
            throw naarVervolgpagina();
        }
        BetaalSoort soort = stand.get().soort();
        switch (stand.get().status()) {
            case GELUKT -> throw new RestartResponseException(AanvraagStatusPage.class,
                    makeDossierPageParameters(dossierId));
            case MISLUKT -> throw new RestartResponseException(soort == BetaalSoort.REGISTRATIE
                    ? ControlerenPage.naMislukteBetaling(dossierId)
                    : AanvraagStatusPage.naMislukteBetaling(dossierId));
            case GESTART -> {
                // Wachten op de uitslag
            }
        }

        langzaam = new Label("langzaam", new ResourceModel("betaling.retour.langzaam"));
        langzaam.setVisible(false);
        langzaam.setOutputMarkupPlaceholderTag(true);
        pageBody.add(
                new RdHeading("heading", getString("betaling.retour.heading"), 1),
                new Label("tekst", new ResourceModel("betaling.retour.tekst")),
                langzaam
        );
        add(new WachtOpUitslag());
    }

    private RestartResponseException naarVervolgpagina() {
        return marriageIntakeService.findStatus(dossierId) == DossierStatus.CONCEPT
                ? new RestartResponseException(ControlerenPage.class, makeDossierPageParameters(dossierId))
                : new RestartResponseException(AanvraagStatusPage.class, makeDossierPageParameters(dossierId));
    }

    /**
     * Laadt de pagina opnieuw zodra de uitslag binnen is. Duurt het lang, dan legt de pagina uit dat de burger
     * niet hoeft te wachten, en stopt met kijken: anders houdt een open tabblad de sessie eindeloos levend.
     */
    private class WachtOpUitslag extends AbstractAjaxTimerBehavior {

        private int rondes;

        WachtOpUitslag() {
            super(WACHTTIJD);
        }

        @Override
        protected void onTimer(AjaxRequestTarget target) {
            BetalingStatus status = onlineBetalenService.findLaatsteBetaling(dossierId).orElseThrow().status();
            if (status != BetalingStatus.GESTART) {
                setResponsePage(BetalingRetourPage.class, makeDossierPageParameters(dossierId));
                return;
            }
            rondes++;
            if (rondes >= LANGZAAM_NA_RONDES) {
                langzaam.setVisible(true);
                target.add(langzaam);
                stop(target);
            }
        }
    }
}

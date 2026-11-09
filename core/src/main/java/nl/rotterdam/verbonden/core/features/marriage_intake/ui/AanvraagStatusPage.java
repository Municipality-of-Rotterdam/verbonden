package nl.rotterdam.verbonden.core.features.marriage_intake.ui;

import nl.rotterdam.nl_design_system.rotterdam_extensions.wicket.components.rotterdam_icon.RotterdamIconBehavior;
import nl.rotterdam.nl_design_system.wicket.components.heading.RdHeading;
import nl.rotterdam.nl_design_system.wicket.components.alert.RdAlert;
import nl.rotterdam.nl_design_system.wicket.components.alert.RdAlertType;
import nl.rotterdam.nl_design_system.wicket.components.button.RdButton;
import nl.rotterdam.verbonden.core.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.verbonden.core.features.marriage_intake.application.OnlineBetalenService;
import nl.rotterdam.verbonden.core.domain.Bedrag;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.BetaalmutatieNietTeBetalenException;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.BetaaloverzichtDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.BetalingLooptAlException;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.TeBetalenDto;
import nl.rotterdam.verbonden.core.online_betalen.OnlineBetalenNietBeschikbaarException;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierSamenvattingDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierStatus;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.LambdaModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wicketstuff.minis.behavior.VisibleModelBehavior;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static nl.rotterdam.verbonden.core.features.marriage_intake.ui.DossierPageParameterUtil.makeDossierPageParameters;

/**
 * Shown once the citizen has submitted the dossier: confirms receipt, explains what happens next,
 * and shows the current status of the application. The dossier can no longer be changed.
 */
public class AanvraagStatusPage extends IntakeBasePage {

    private static final DateTimeFormatter DATUM_FORMAT =
            DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.forLanguageTag("nl-NL"));

    private static final DateTimeFormatter TIJD_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private static final Logger LOG = LoggerFactory.getLogger(AanvraagStatusPage.class);

    @SpringBean
    private MarriageIntakeService marriageIntakeService;

    @SpringBean
    private OnlineBetalenService onlineBetalenService;

    // Eenmalige melding: wordt na het renderen gewist, zodat hij na een verversing niet opnieuw verschijnt
    private boolean betalingMislukt;

    private final IModel<DossierSamenvattingDto> dossierModel =
            LoadableDetachableModel.of(() -> marriageIntakeService.findByDossierId(dossierId));

    /**
     * De statuspagina met een melding dat de betaling van een betaalmutatie niet is gelukt.
     */
    static AanvraagStatusPage naMislukteBetaling(UUID dossierId) {
        AanvraagStatusPage page = new AanvraagStatusPage();
        page.getPageParameters().mergeWith(makeDossierPageParameters(dossierId));
        page.betalingMislukt = true;
        return page;
    }

    public static void respond(UUID dossierId) {
        RequestCycle.get().setResponsePage(AanvraagStatusPage.class, makeDossierPageParameters(dossierId));
    }

    @Override
    protected IntakeStep getActiveStep() {
        return null;
    }

    @Override
    protected boolean hasIntakeStepLayout() {
        return false;
    }

    @Override
    protected IModel<String> getTitleModel() {
        return new ResourceModel("aanvraag.status.page.title");
    }

    @Override
    protected boolean isToegestaanNaIndienen() {
        return true;
    }

    @Override
    protected IModel<DossierSamenvattingDto> getSidebarDossierModel() {
        return dossierModel;
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        DossierSamenvattingDto dossier = dossierModel.getObject();
        DossierStatus status = dossier.status();
        if (status == DossierStatus.CONCEPT) {
            // Nothing submitted yet: continue with the intake
            throw new RestartResponseException(DeDagPage.class, makeDossierPageParameters(dossierId));
        }

        WebMarkupContainer hero = new WebMarkupContainer("hero");
        hero.add(AttributeModifier.append("class", "rd-aanvraag-hero--" + status.name().toLowerCase(Locale.ROOT)));
        hero.add(
                new WebMarkupContainer("heroIcon").add(status == DossierStatus.AFGEWEZEN
                        ? RotterdamIconBehavior.BADGE_ALERT
                        : RotterdamIconBehavior.BADGE_CHECK),
                new RdHeading("heading", getString("aanvraag.status." + status.name() + ".heading"), 1),
                new Label("tekst", new ResourceModel("aanvraag.status." + status.name() + ".tekst")),
                new Label("ingediendOp", dossier.ingediendOp() != null ? ingediendOpModel(dossier.ingediendOp()) : Model.of(""))
                        .setVisible(dossier.ingediendOp() != null)
        );
        pageBody.add(hero);

        IModel<List<TeBetalenDto>> teBetalenModel =
                LoadableDetachableModel.of(() -> onlineBetalenService.findTeBetalen(dossierId));
        pageBody.add(
                new RdAlert("betalingMislukt", new ResourceModel("aanvraag.status.betaling.mislukt"), RdAlertType.WARNING)
                        .add(new VisibleModelBehavior(LambdaModel.of(() -> betalingMislukt))),
                new WebMarkupContainer("teBetalen")
                        .add(new TeBetalenListView("mutaties", teBetalenModel))
                        .add(new VisibleModelBehavior(teBetalenModel.map(lijst -> !lijst.isEmpty())))
        );

        // "Wat gebeurt er nu?" — not applicable once the application has been rejected
        WebMarkupContainer vervolg = new WebMarkupContainer("vervolg");
        vervolg.setVisible(status != DossierStatus.AFGEWEZEN);
        vervolg.add(
                stap("stapIngediend", StapStatus.GEDAAN),
                betalingStap(),
                stap("stapBeoordeling", status == DossierStatus.GEACCEPTEERD ? StapStatus.GEDAAN : StapStatus.HUIDIG)
                        .add(new Label("beoordelingTitel",
                                new ResourceModel("aanvraag.status.stap.beoordeling.titel." + status.name())))
                        .add(new Label("beoordelingTekst",
                                new ResourceModel("aanvraag.status.stap.beoordeling.tekst." + status.name()))),
                stap("stapDeDag", StapStatus.NOG_NIET)
                        .add(new Label("deDagTekst", deDagModel(dossier)))
        );
        pageBody.add(vervolg);
    }

    @Override
    protected void onAfterRender() {
        super.onAfterRender();
        betalingMislukt = false;
    }

    /**
     * Elke openstaande betaalmutatie met een eigen knop: een betaling hoort bij precies één mutatie.
     */
    private class TeBetalenListView extends ListView<TeBetalenDto> {

        TeBetalenListView(String id, IModel<List<TeBetalenDto>> model) {
            super(id, model);
        }

        @Override
        protected void populateItem(ListItem<TeBetalenDto> item) {
            TeBetalenDto mutatie = item.getModelObject();
            item.add(
                    new Label("omschrijving", mutatie.omschrijving()),
                    new Label("bedrag", "€ " + mutatie.bedrag().format()),
                    new Form<Void>("betalenForm").add(new BetalenButton("betalen", mutatie.betaalmutatieId()))
                            .setVisible(!mutatie.inBehandeling()),
                    new Label("inBehandeling", new ResourceModel("aanvraag.status.tebetalen.inbehandeling"))
                            .setVisible(mutatie.inBehandeling())
            );
        }
    }

    private class BetalenButton extends RdButton {

        private final long betaalmutatieId;

        BetalenButton(String id, long betaalmutatieId) {
            super(id, new ResourceModel("aanvraag.status.tebetalen.betalen"));
            this.betaalmutatieId = betaalmutatieId;
        }

        @Override
        public void onSubmit() {
            String redirectUrl;
            try {
                redirectUrl = onlineBetalenService.startMutatieBetaling(dossierId, betaalmutatieId, getCurrentBsn(),
                        BetalingRetourUrl.voor(dossierId));
            } catch (OnlineBetalenNietBeschikbaarException e) {
                LOG.warn("Betaling van betaalmutatie {} voor dossier {} kon niet starten", betaalmutatieId, dossierId, e);
                setResponsePage(naMislukteBetaling(dossierId));
                return;
            } catch (BetaalmutatieNietTeBetalenException | BetalingLooptAlException e) {
                // Intussen al betaald, in behandeling of verwijderd (bijv. door de andere partner of een
                // medewerker): toon de actuele stand
                respond(dossierId);
                return;
            }
            throw new RedirectToUrlException(redirectUrl);
        }
    }

    /**
     * Wat het paar heeft betaald en nog moet betalen. Zonder betalingen en zonder openstaand bedrag (gratis, of
     * ingediend en afgerekend aan de balie) is er niets te melden.
     */
    private Component betalingStap() {
        BetaaloverzichtDto overzicht = onlineBetalenService.findBetaaloverzicht(dossierId);
        BetaaloverzichtDto.Stand stand = overzicht.stand();
        WebMarkupContainer stap = stap("stapBetaling",
                stand == BetaaloverzichtDto.Stand.BETAALD ? StapStatus.GEDAAN : StapStatus.HUIDIG);
        stap.setVisible(stand != BetaaloverzichtDto.Stand.GEEN);
        if (stand == BetaaloverzichtDto.Stand.GEEN) {
            return stap.add(new Label("betalingTitel", ""), new Label("betalingTekst", ""));
        }

        String sleutel = "aanvraag.status.stap.betaling." + stand.name();
        Bedrag bedrag = switch (stand) {
            case BETAALD -> overzicht.betaald();
            case OPEN -> overzicht.open();
            case NIET_GELUKT -> overzicht.nietGelukt();
            case IN_BEHANDELING -> overzicht.inBehandeling();
            default -> throw new IllegalStateException("Unexpected value: " + stand);
        };
        String tekst = new StringResourceModel(sleutel + ".tekst", this).setParameters(euro(bedrag)).getObject();
        if (stand != BetaaloverzichtDto.Stand.BETAALD && overzicht.betaald().isPositive()) {
            tekst = new StringResourceModel("aanvraag.status.stap.betaling.betaald", this)
                    .setParameters(euro(overzicht.betaald())).getObject() + " " + tekst;
        }
        return stap.add(
                new Label("betalingTitel", new ResourceModel(sleutel + ".titel")),
                new Label("betalingTekst", tekst));
    }

    private static String euro(Bedrag bedrag) {
        return "€ " + bedrag.format();
    }

    private enum StapStatus {
        GEDAAN, HUIDIG, NOG_NIET
    }

    private static WebMarkupContainer stap(String id, StapStatus stapStatus) {
        WebMarkupContainer stap = new WebMarkupContainer(id);
        stap.add(AttributeModifier.append("class", switch (stapStatus) {
            case GEDAAN -> "rd-aanvraag-stap--gedaan";
            case HUIDIG -> "rd-aanvraag-stap--huidig";
            case NOG_NIET -> "rd-aanvraag-stap--nog-niet";
        }));
        if (stapStatus == StapStatus.HUIDIG) {
            stap.add(AttributeModifier.replace("aria-current", "step"));
        }
        return stap;
    }

    private IModel<String> ingediendOpModel(LocalDateTime ingediendOp) {
        return new StringResourceModel("aanvraag.status.ingediend.op", this)
                .setParameters(ingediendOp.format(DATUM_FORMAT), ingediendOp.format(TIJD_FORMAT));
    }

    private IModel<String> deDagModel(DossierSamenvattingDto dossier) {
        LocalDateTime datumTijd = dossier.datumTijdHuwelijk();
        if (datumTijd == null) {
            return Model.of("");
        }
        return dossier.huwelijksLocatie() != null
                ? new StringResourceModel("aanvraag.status.stap.dedag.tekst.locatie", this)
                        .setParameters(datumTijd.format(DATUM_FORMAT), datumTijd.format(TIJD_FORMAT), dossier.huwelijksLocatie())
                : new StringResourceModel("aanvraag.status.stap.dedag.tekst", this)
                        .setParameters(datumTijd.format(DATUM_FORMAT), datumTijd.format(TIJD_FORMAT));
    }
}

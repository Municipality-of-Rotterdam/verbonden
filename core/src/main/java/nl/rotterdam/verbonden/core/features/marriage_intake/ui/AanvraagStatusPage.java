package nl.rotterdam.verbonden.core.features.marriage_intake.ui;

import nl.rotterdam.nl_design_system.rotterdam_extensions.wicket.components.rotterdam_icon.RotterdamIconBehavior;
import nl.rotterdam.nl_design_system.wicket.components.heading.RdHeading;
import nl.rotterdam.verbonden.core.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierSamenvattingDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierStatus;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

import static nl.rotterdam.verbonden.core.features.marriage_intake.ui.DossierPageParameterUtil.makeDossierPageParameters;

/**
 * Shown once the citizen has submitted the dossier: confirms receipt, explains what happens next
 * and shows the current status of the application. The dossier can no longer be changed.
 */
public class AanvraagStatusPage extends IntakeBasePage {

    private static final DateTimeFormatter DATUM_FORMAT =
            DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.forLanguageTag("nl-NL"));

    private static final DateTimeFormatter TIJD_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    @SpringBean
    private MarriageIntakeService marriageIntakeService;

    private final IModel<DossierSamenvattingDto> dossierModel =
            LoadableDetachableModel.of(() -> marriageIntakeService.findByDossierId(dossierId));

    public static void respond(UUID dossierId) {
        RequestCycle.get().setResponsePage(AanvraagStatusPage.class, makeDossierPageParameters(dossierId));
    }

    @Override
    protected IntakeStep getActiveStep() {
        return null;
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

        // "Wat gebeurt er nu?" — not applicable once the application has been rejected
        WebMarkupContainer vervolg = new WebMarkupContainer("vervolg");
        vervolg.setVisible(status != DossierStatus.AFGEWEZEN);
        vervolg.add(
                stap("stapIngediend", StapStatus.GEDAAN),
                stap("stapBeoordeling", status == DossierStatus.GEACCEPTEERD ? StapStatus.GEDAAN : StapStatus.HUIDIG)
                        .add(new Label("beoordelingTitel",
                                new ResourceModel("aanvraag.status.stap.beoordeling.titel." + status.name())))
                        .add(new Label("beoordelingTekst",
                                new ResourceModel("aanvraag.status.stap.beoordeling.tekst." + status.name()))),
                stap("stapFactuur", status == DossierStatus.GEACCEPTEERD ? StapStatus.HUIDIG : StapStatus.NOG_NIET),
                stap("stapDeDag", StapStatus.NOG_NIET)
                        .add(new Label("deDagTekst", deDagModel(dossier)))
        );
        pageBody.add(vervolg);
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

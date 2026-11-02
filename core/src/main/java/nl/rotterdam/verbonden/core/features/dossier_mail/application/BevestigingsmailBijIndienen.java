package nl.rotterdam.verbonden.core.features.dossier_mail.application;

import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierIngediendEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Legt bij het indienen van een dossier de bevestigingsmail vast, binnen dezelfde transactie: een ingediend dossier
 * heeft dus altijd een bevestigingsmail.
 */
@Component
class BevestigingsmailBijIndienen {

    private final DossierMailService dossierMailService;

    BevestigingsmailBijIndienen(DossierMailService dossierMailService) {
        this.dossierMailService = dossierMailService;
    }

    @EventListener
    void bijIndienen(DossierIngediendEvent event) {
        dossierMailService.maakBevestigingsmail(event.dossierId());
    }
}

package nl.rotterdam.verbonden.core.features.dossier_mail.application;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Zoekt op vaste momenten naar e-mails die verstuurd moeten worden, of waarvan een nieuwe poging aan de beurt is.
 */
@Component
class DossierMailVerwerker {

    private final DossierMailService dossierMailService;

    DossierMailVerwerker(DossierMailService dossierMailService) {
        this.dossierMailService = dossierMailService;
    }

    @Scheduled(fixedDelayString = "#{@mailConfig.verwerkInterval.toMillis()}")
    void verwerk() {
        dossierMailService.verwerkOpenstaandeMails();
    }
}

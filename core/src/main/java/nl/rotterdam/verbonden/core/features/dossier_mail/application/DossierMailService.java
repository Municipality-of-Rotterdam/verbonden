package nl.rotterdam.verbonden.core.features.dossier_mail.application;

import java.util.UUID;

/**
 * Het versturen van e-mails over een dossier. Elke e-mail wordt met inhoud, ontvangers en elke verzendpoging bij
 * het dossier vastgelegd, ook als het verzenden mislukt.
 */
public interface DossierMailService {

    /**
     * Maakt voor een ingediend dossier een bevestigingsmail per partner met een e-mailadres, en legt die vast zodat
     * hij wordt verzonden door {@link #verwerkOpenstaandeMails()}. Heeft geen van de partners een e-mailadres, of
     * lukt het opstellen van de e-mail niet, dan wordt de e-mail vastgelegd als mislukt; het indienen gaat door.
     *
     * @throws IllegalArgumentException wanneer het dossier niet bestaat
     */
    void maakBevestigingsmail(UUID dossierId);

    /**
     * Verstuurt alle vastgelegde e-mails waarvan de volgende verzendpoging aan de beurt is. Een mislukte poging
     * wordt volgens de instellingen {@code verbonden.mail.*} opnieuw geprobeerd, tenzij de mailserver het adres
     * definitief afwijst.
     *
     * @return het aantal e-mails waarvoor een poging is gedaan
     */
    int verwerkOpenstaandeMails();
}

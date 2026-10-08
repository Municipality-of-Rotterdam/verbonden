package nl.rotterdam.verbonden.core.features.dossier_administration.domain;

import nl.rotterdam.verbonden.core.domain.BurgerServiceNummer;

/**
 * Bij het BSN van een partner is in de BRP geen persoon gevonden.
 */
public class PartnerNietInBrpException extends RuntimeException {

    public PartnerNietInBrpException(BurgerServiceNummer bsn) {
        super("Geen persoon gevonden in de BRP voor BSN " + bsn.getValue());
    }
}

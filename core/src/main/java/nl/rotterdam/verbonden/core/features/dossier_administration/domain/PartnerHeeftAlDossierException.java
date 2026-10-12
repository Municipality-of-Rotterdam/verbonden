package nl.rotterdam.verbonden.core.features.dossier_administration.domain;

import nl.rotterdam.verbonden.core.domain.BurgerServiceNummer;

/**
 * Een partner kan maar in één actief dossier staan; met dit BSN bestaat al een actief dossier.
 */
public class PartnerHeeftAlDossierException extends RuntimeException {

    public PartnerHeeftAlDossierException(BurgerServiceNummer bsn) {
        super("Er bestaat al een dossier voor BSN " + bsn.getValue());
    }
}

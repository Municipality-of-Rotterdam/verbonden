package nl.rotterdam.verbonden.core.features.marriage_intake.domain;

import java.util.UUID;

/**
 * Gegooid wanneer een dossier gewijzigd wordt dat niet (meer) de vereiste status heeft, bijvoorbeeld
 * omdat de burger het dossier al heeft ingediend.
 */
public class DossierNietWijzigbaarException extends IllegalStateException {

    public DossierNietWijzigbaarException(UUID dossierId, DossierStatus huidigeStatus, DossierStatus vereisteStatus) {
        super("Dossier " + dossierId + " heeft status " + huidigeStatus + "; vereist is " + vereisteStatus);
    }
}

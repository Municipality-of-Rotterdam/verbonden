package nl.rotterdam.verbonden.core.features.marriage_intake.domain;

import java.util.UUID;

/**
 * Gegooid wanneer een dossier wordt ingediend terwijl nog niet alle keuzes en gegevens zijn ingevuld.
 */
public class DossierNietCompleetException extends IllegalStateException {

    public DossierNietCompleetException(UUID dossierId) {
        super("Dossier " + dossierId + " is nog niet compleet en kan niet worden ingediend");
    }
}

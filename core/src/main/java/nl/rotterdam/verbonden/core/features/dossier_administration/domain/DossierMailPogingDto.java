package nl.rotterdam.verbonden.core.features.dossier_administration.domain;

import nl.rotterdam.verbonden.core.features.dossier_mail.domain.DossierMailPogingStatus;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @param foutmelding {@code null} bij een geslaagde poging
 */
public record DossierMailPogingDto(
        int volgnummer,
        LocalDateTime tijdstip,
        DossierMailPogingStatus status,
        String foutmelding
) implements Serializable {
}

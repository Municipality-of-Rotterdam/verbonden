package nl.rotterdam.verbonden.core.features.dossier_administration.domain;

import nl.rotterdam.verbonden.core.features.dossier_mail.domain.DossierMailStatus;
import nl.rotterdam.verbonden.core.features.dossier_mail.domain.DossierMailType;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Een verstuurde e-mail met de inhoud zoals die is verstuurd en elke verzendpoging.
 *
 * @param inhoud           de HTML van de e-mail
 * @param volgendePogingOp {@code null} wanneer er geen nieuwe poging meer volgt
 */
public record DossierMailDto(
        long id,
        DossierMailType type,
        String afzender,
        List<String> ontvangers,
        String onderwerp,
        String inhoud,
        DossierMailStatus status,
        LocalDateTime aangemaaktOp,
        LocalDateTime volgendePogingOp,
        List<DossierMailPogingDto> pogingen
) implements Serializable {
}

package nl.rotterdam.verbonden.core.features.dossier_administration.domain;

import nl.rotterdam.verbonden.core.features.dossier_mail.domain.DossierMailStatus;
import nl.rotterdam.verbonden.core.features.dossier_mail.domain.DossierMailType;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @param laatstePogingOp   {@code null} zolang er nog geen poging is gedaan
 * @param volgendePogingOp  {@code null} wanneer er geen nieuwe poging meer volgt
 */
public record ListDossierMailDto(
        long id,
        DossierMailType type,
        String onderwerp,
        List<String> ontvangers,
        DossierMailStatus status,
        LocalDateTime aangemaaktOp,
        int aantalPogingen,
        LocalDateTime laatstePogingOp,
        LocalDateTime volgendePogingOp
) implements Serializable {
}

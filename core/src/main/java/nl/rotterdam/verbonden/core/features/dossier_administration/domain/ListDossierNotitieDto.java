package nl.rotterdam.verbonden.core.features.dossier_administration.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

public record ListDossierNotitieDto(
        long id,
        String tekst,
        String gebruikersnaam,
        String weergavenaam,
        LocalDateTime aangemaaktOp
) implements Serializable {
}

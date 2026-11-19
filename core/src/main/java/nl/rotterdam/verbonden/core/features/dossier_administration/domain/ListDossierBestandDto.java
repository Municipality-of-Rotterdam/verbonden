package nl.rotterdam.verbonden.core.features.dossier_administration.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

public record ListDossierBestandDto(
        long id,
        String bestandsnaam,
        DossierBestandType bestandType,
        DossierBijlageType bijlageType,
        String opmerking,
        long grootte,
        String toegevoegdDoor,
        LocalDateTime toegevoegdOp
) implements Serializable {
}

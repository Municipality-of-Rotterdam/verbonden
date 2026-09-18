package nl.rotterdam.verbonden.core.features.dossier_administration.domain;

import nl.rotterdam.verbonden.core.domain.BurgerServiceNummer;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.RegistratieType;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public record ListDossierDto(
        UUID dossierId,
        BurgerServiceNummer bsn1,
        BurgerServiceNummer bsn2,
        RegistratieType registratieType,
        CeremonieSoort ceremonieSoort,
        LocalDateTime aangemaaktOp
) implements Serializable {
}

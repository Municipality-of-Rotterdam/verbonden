package nl.rotterdam.verbonden.core.features.dossier_administration.domain;

import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.RegistratieType;

public record ChangeCeremonieDto(
        RegistratieType registratieType,
        CeremonieSoort ceremonieSoort
) {
}

package nl.rotterdam.verbonden.core.features.marriage_intake.domain;

import nl.rotterdam.verbonden.core.domain.BurgerServiceNummer;

public record CreateDossierDto(
        RegistratieType registratieType,
        CeremonieSoort ceremonieSoort,
        Long locatieId,
        BurgerServiceNummer bsn1
) {
}

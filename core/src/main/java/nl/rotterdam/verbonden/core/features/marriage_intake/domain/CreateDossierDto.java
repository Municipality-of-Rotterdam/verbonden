package nl.rotterdam.verbonden.core.features.marriage_intake.domain;

public record CreateDossierDto(
        RegistratieType registratieType,
        CeremonieSoort ceremonieSoort,
        Long locatieId,
        String bsn1
) {
}

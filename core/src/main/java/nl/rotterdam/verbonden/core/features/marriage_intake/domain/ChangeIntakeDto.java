package nl.rotterdam.verbonden.core.features.marriage_intake.domain;

public record ChangeIntakeDto(
        RegistratieType registratieType,
        CeremonieSoort ceremonieSoort,
        Long locatieId
) {
}

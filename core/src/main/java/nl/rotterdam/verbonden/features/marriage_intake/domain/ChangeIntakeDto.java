package nl.rotterdam.verbonden.features.marriage_intake.domain;

public record ChangeIntakeDto(
        RegistratieType registratieType,
        CeremonieSoort ceremonieSoort,
        Long locatieId
) {
}

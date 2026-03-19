package nl.rotterdam.huwelijk.features.marriage_intake.domain;

public record DossierSamenvattingDto(
        long id,
        RegistratieType registratieType,
        CeremonieSoort ceremonieSoort
) {
}

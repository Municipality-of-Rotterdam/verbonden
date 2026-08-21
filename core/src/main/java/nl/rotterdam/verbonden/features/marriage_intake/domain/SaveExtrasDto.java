package nl.rotterdam.verbonden.features.marriage_intake.domain;

public record SaveExtrasDto(
        boolean ringenUitwisselen,
        boolean muziek,
        Long trouwboekjeId,
        Long internationaleAkteId
) {
}

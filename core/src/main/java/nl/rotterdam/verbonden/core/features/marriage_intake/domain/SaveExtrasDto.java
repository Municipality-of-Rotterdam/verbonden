package nl.rotterdam.verbonden.core.features.marriage_intake.domain;

public record SaveExtrasDto(
        boolean ringenUitwisselen,
        boolean muziek,
        Long trouwboekjeId,
        Long internationaleAkteId
) {
}

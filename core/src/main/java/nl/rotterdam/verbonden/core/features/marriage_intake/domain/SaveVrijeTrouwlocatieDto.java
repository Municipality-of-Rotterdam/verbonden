package nl.rotterdam.verbonden.core.features.marriage_intake.domain;

import nl.rotterdam.verbonden.core.domain.Telefoonnummer;

public record SaveVrijeTrouwlocatieDto(
        String naam,
        String adres,
        Telefoonnummer telefoonnummer,
        String contactpersoon
) {
}

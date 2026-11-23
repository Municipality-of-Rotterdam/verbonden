package nl.rotterdam.verbonden.core.features.marriage_intake.domain;

import nl.rotterdam.verbonden.core.domain.Telefoonnummer;

import java.io.Serializable;

public record VrijeTrouwlocatieDto(
        String naam,
        String adres,
        Telefoonnummer telefoonnummer,
        String contactpersoon,
        boolean goedgekeurd
) implements Serializable {
}

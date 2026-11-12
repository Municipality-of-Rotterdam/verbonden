package nl.rotterdam.verbonden.core.features.marriage_intake.domain;

import nl.rotterdam.verbonden.core.domain.Bedrag;

import java.io.Serializable;

public record BeschikbareLocatieDto(
        long id,
        String naam,
        String fotoUrl,
        String omschrijving,
        String detailUrl,
        boolean vrijeLocatie,
        Bedrag prijs
) implements Serializable {
}

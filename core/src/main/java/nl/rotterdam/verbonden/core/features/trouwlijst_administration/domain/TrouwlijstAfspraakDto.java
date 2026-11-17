package nl.rotterdam.verbonden.core.features.trouwlijst_administration.domain;

import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.RegistratieType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Resultaat van de trouwlijst-query: alleen de velden die de lijst nodig heeft.
 */
public record TrouwlijstAfspraakDto(
        UUID dossierId,
        LocalDate datum,
        LocalTime startTijd,
        String locatie,
        RegistratieType registratieType,
        CeremonieSoort ceremonieSoort
) {
}

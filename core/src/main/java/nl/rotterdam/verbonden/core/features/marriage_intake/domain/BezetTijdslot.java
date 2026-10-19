package nl.rotterdam.verbonden.core.features.marriage_intake.domain;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Een tijdslot waarop een locatie al een afspraak heeft, met het dossier dat die afspraak heeft.
 */
public record BezetTijdslot(long locatieId, LocalDate datum, LocalTime startTijd, long dossierId) {
}

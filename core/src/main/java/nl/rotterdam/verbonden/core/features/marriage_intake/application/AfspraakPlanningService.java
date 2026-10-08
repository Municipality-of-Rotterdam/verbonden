package nl.rotterdam.verbonden.core.features.marriage_intake.application;

import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CeremonieSoort;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Vrije tijdsloten voor een ceremonie, en het boeken van een afspraak daarop. Kijkt niet naar de status van het
 * dossier: of het dossier gewijzigd mag worden, bepaalt de aanroeper (de burger alleen bij een concept, een
 * medewerker altijd).
 */
public interface AfspraakPlanningService {

    /**
     * De eerste dag (vanaf morgen, tot een jaar vooruit) met een vrij tijdslot, of {@code null} als die er niet is.
     */
    LocalDate findEersteGelegenheid(CeremonieSoort ceremonieSoort);

    Set<LocalDate> findBeschikbareDatums(CeremonieSoort ceremonieSoort, YearMonth maand);

    List<LocalDateTime> findBeschikbareSlots(CeremonieSoort ceremonieSoort, YearMonth maand);

    /**
     * Alle vrije tijdsloten binnen de planningsperiode.
     */
    List<LocalDateTime> findAllBeschikbareSlots(CeremonieSoort ceremonieSoort);

    List<LocalTime> findBeschikbareTijden(CeremonieSoort ceremonieSoort, LocalDate datum);

    /**
     * Boekt een afspraak voor het dossier op een vrij tijdslot, en vervangt daarmee een eerdere afspraak.
     *
     * @throws IllegalStateException wanneer er op dat moment geen vrij tijdslot is
     */
    void boekAfspraak(UUID dossierId, LocalDate datum, LocalTime startTijd);
}

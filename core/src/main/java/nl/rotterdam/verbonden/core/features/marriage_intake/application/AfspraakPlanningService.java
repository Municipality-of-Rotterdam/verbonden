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
 *
 * <p>Voor een dossier telt het tijdslot dat het zelf al heeft geboekt als vrij, zodat het opnieuw gekozen en
 * bevestigd kan worden.</p>
 */
public interface AfspraakPlanningService {

    /**
     * De eerste dag binnen de planningsperiode met een vrij tijdslot, of {@code null} als die er niet is. Dat is
     * dezelfde periode waarin de burger een datum kan kiezen.
     */
    LocalDate findEersteGelegenheid(CeremonieSoort ceremonieSoort);

    Set<LocalDate> findBeschikbareDatums(UUID dossierId, YearMonth maand);

    List<LocalDateTime> findBeschikbareSlots(UUID dossierId, YearMonth maand);

    /**
     * Alle vrije tijdsloten binnen de planningsperiode.
     */
    List<LocalDateTime> findAllBeschikbareSlots(UUID dossierId);

    List<LocalTime> findBeschikbareTijden(UUID dossierId, LocalDate datum);

    /**
     * Boekt een afspraak voor het dossier op een vrij tijdslot, en vervangt daarmee een eerdere afspraak.
     *
     * @throws IllegalStateException wanneer er op dat moment geen vrij tijdslot is
     */
    void boekAfspraak(UUID dossierId, LocalDate datum, LocalTime startTijd);
}

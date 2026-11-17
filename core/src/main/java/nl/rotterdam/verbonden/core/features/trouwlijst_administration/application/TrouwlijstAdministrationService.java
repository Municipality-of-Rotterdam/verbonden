package nl.rotterdam.verbonden.core.features.trouwlijst_administration.application;

import nl.rotterdam.verbonden.core.features.trouwlijst_administration.domain.ListTrouwlijstDossierDto;
import nl.rotterdam.verbonden.core.features.trouwlijst_administration.domain.TrouwlijstWeekDto;

import java.time.LocalDate;
import java.util.List;

public interface TrouwlijstAdministrationService {

    /**
     * De weken waarvoor een trouwlijst gemaakt kan worden: de vorige week, de huidige week en de 51 weken daarna.
     */
    List<TrouwlijstWeekDto> findWeken();

    /**
     * De bevestigde dossiers met een trouwdatum tussen {@code van} en {@code tot} (inclusief), op datum, locatie
     * en tijd.
     */
    List<ListTrouwlijstDossierDto> findBevestigdeDossiers(LocalDate van, LocalDate tot);

    /**
     * De trouwlijst als Excel-bestand (xlsx), gegroepeerd per datum en locatie.
     */
    byte[] exporteer(LocalDate van, LocalDate tot);
}

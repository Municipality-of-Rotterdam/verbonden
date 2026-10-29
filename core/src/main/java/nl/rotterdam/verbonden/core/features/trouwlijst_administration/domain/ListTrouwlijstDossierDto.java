package nl.rotterdam.verbonden.core.features.trouwlijst_administration.domain;

import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.RegistratieType;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * Een bevestigd dossier op de trouwlijst.
 *
 * @param babs     de toegewezen BABS, of {@code null}: dit is nog niet vastgelegd in het dossier
 * @param taal     de taal van de ceremonie, of {@code null}: dit is nog niet vastgelegd in het dossier
 * @param partners de partners in volgorde
 */
public record ListTrouwlijstDossierDto(
        UUID dossierId,
        LocalDate datum,
        LocalTime startTijd,
        String locatie,
        String babs,
        RegistratieType registratieType,
        CeremonieSoort ceremonieSoort,
        List<TrouwlijstPartnerDto> partners,
        String taal
) implements Serializable {
}

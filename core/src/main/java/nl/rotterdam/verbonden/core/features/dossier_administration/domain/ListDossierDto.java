package nl.rotterdam.verbonden.core.features.dossier_administration.domain;

import nl.rotterdam.verbonden.core.domain.BuitenlandsPersoonsnummer;
import nl.rotterdam.verbonden.core.domain.BurgerServiceNummer;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.AanmaakKanaal;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierStatus;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.RegistratieType;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @param persoonsnummer2 het persoonsnummer uit het paspoort van partner 2, wanneer die geen BSN heeft
 */
public record ListDossierDto(
        UUID dossierId,
        BurgerServiceNummer bsn1,
        BurgerServiceNummer bsn2,
        BuitenlandsPersoonsnummer persoonsnummer2,
        RegistratieType registratieType,
        CeremonieSoort ceremonieSoort,
        AanmaakKanaal kanaal,
        LocalDateTime aangemaaktOp,
        DossierStatus status,
        LocalDateTime ingediendOp
) implements Serializable {
}

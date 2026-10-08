package nl.rotterdam.verbonden.core.features.dossier_administration.domain;

import nl.rotterdam.verbonden.core.features.marriage_intake.domain.AanmaakKanaal;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierStatus;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.GetuigeDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.RegistratieType;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.SidebarExtraItemDto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Alle gegevens van een dossier, voor de detailpagina in beheer.
 *
 * @param aangemaaktDoor de medewerker die het dossier aanmaakte, of {@code null} bij {@link AanmaakKanaal#ONLINE}
 * @param extras         de gekozen extra's met hun prijs, voor de weergave
 * @param extraKeuzes    dezelfde keuzes, als uitgangspunt om ze te wijzigen
 * @param compleet       of alles is ingevuld wat nodig is om het dossier in te dienen
 */
public record DossierDetailDto(
        UUID dossierId,
        DossierStatus status,
        AanmaakKanaal kanaal,
        String aangemaaktDoor,
        LocalDateTime aangemaaktOp,
        LocalDateTime ingediendOp,
        RegistratieType registratieType,
        CeremonieSoort ceremonieSoort,
        String locatieNaam,
        LocalDateTime datumTijdHuwelijk,
        BigDecimal ceremoniePrijs,
        List<DetailPartnerDto> partners,
        List<GetuigeDto> getuigen,
        List<SidebarExtraItemDto> extras,
        ChangeExtrasDto extraKeuzes,
        BigDecimal totaalPrijs,
        boolean compleet,
        List<ListDossierBestandDto> bestanden
) implements Serializable {
}

package nl.rotterdam.huwelijk.features.marriage_intake.application;

import nl.rotterdam.huwelijk.features.marriage_intake.domain.ChangeIntakeDto;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.CreateDossierDto;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.DossierAccessOutcome;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.DossierSamenvattingDto;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.IntakeMarriageTypeDto;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.PartnerGegevensDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface MarriageIntakeService {

    List<IntakeMarriageTypeDto> findAllMarriageTypes();

    List<PartnerGegevensDto> findPartnerGegevens(UUID dossierId);

    UUID create(CreateDossierDto dto);

    void updateCeremonie(UUID dossierId, CeremonieSoort ceremonieSoort);

    void updateIntake(UUID dossierId, ChangeIntakeDto dto);

    /**
     * Returns the UUID of the dossier in which the given BSN appears as bsn1 or bsn2,
     * or {@link Optional#empty()} when no such dossier exists.
     */
    Optional<UUID> findDossierIdByBsn(String bsn);

    /**
     * Validates that the given BSN has access to the dossier.
     * <ul>
     *   <li>If bsn matches bsn1 or bsn2: access granted, no changes.</li>
     *   <li>If bsn2 is null and bsn is different from bsn1: bsn is registered as bsn2, access granted.</li>
     *   <li>Otherwise: {@link IllegalStateException} is thrown.</li>
     * </ul>
     */
    void ensureBsnAccess(UUID dossierId, String bsn);

    /**
     * Determines and grants (or denies) access to a requested dossier for the given BSN.
     * <ul>
     *   <li>If bsn matches bsn1 or bsn2 of the requested dossier:
     *       returns {@link DossierAccessOutcome.Scenario#GRANTED} with the requested dossier ID.</li>
     *   <li>If bsn belongs to a different existing dossier:
     *       returns {@link DossierAccessOutcome.Scenario#SWITCHED_DOSSIER} with that dossier's ID.</li>
     *   <li>If bsn is not in any dossier and the requested dossier has no bsn2 yet:
     *       registers bsn as bsn2 and returns {@link DossierAccessOutcome.Scenario#GRANTED}.</li>
     *   <li>If bsn is not in any dossier and the requested dossier already has two BSNs:
     *       returns {@link DossierAccessOutcome.Scenario#NOT_AUTHORIZED} with a {@code null} dossier ID.</li>
     * </ul>
     */
    DossierAccessOutcome resolveAccess(UUID requestedDossierId, String bsn);

    DossierSamenvattingDto findByDossierId(UUID id);

    Set<LocalDate> findBeschikbareDatums(UUID dossierId, YearMonth maand);

    List<LocalDateTime> findBeschikbareSlots(UUID dossierId, YearMonth maand);

    List<LocalTime> findBeschikbareTijden(UUID dossierId, LocalDate datum);

    void slaAfspraakOp(UUID dossierId, LocalDate datum, LocalTime startTijd);
}

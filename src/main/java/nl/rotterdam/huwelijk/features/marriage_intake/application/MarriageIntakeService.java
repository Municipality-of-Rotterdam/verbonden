package nl.rotterdam.huwelijk.features.marriage_intake.application;

import nl.rotterdam.huwelijk.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.CreateDossierDto;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.DossierSamenvattingDto;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.IntakeMarriageTypeDto;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.PartnerGegevensDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface MarriageIntakeService {

    List<IntakeMarriageTypeDto> findAllMarriageTypes();

    List<PartnerGegevensDto> findPartnerGegevens(UUID dossierId, String bsn);

    UUID create(CreateDossierDto dto);

    void updateCeremonie(UUID dossierId, CeremonieSoort ceremonieSoort);

    DossierSamenvattingDto findByDossierId(UUID id);

    Set<LocalDate> findBeschikbareDatums(UUID dossierId, YearMonth maand);

    List<LocalDateTime> findBeschikbareSlots(UUID dossierId, YearMonth maand);

    List<LocalTime> findBeschikbareTijden(UUID dossierId, LocalDate datum);

    void slaAfspraakOp(UUID dossierId, LocalDate datum, LocalTime startTijd);
}

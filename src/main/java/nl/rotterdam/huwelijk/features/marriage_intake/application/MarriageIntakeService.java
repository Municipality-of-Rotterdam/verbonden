package nl.rotterdam.huwelijk.features.marriage_intake.application;

import nl.rotterdam.huwelijk.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.CreateDossierDto;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.DossierSamenvattingDto;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.IntakeMarriageTypeDto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface MarriageIntakeService {

    List<IntakeMarriageTypeDto> findAllMarriageTypes();

    UUID create(CreateDossierDto dto);

    void updateCeremonie(UUID dossierId, CeremonieSoort ceremonieSoort);

    DossierSamenvattingDto findByDossierId(UUID id);

    Set<LocalDate> findBeschikbareDatums(UUID dossierId, YearMonth maand);

    List<LocalTime> findBeschikbareTijden(UUID dossierId, LocalDate datum);

    void slaAfspraakOp(UUID dossierId, LocalDate datum, LocalTime startTijd);
}

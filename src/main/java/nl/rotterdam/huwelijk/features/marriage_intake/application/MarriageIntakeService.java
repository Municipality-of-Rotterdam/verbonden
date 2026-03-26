package nl.rotterdam.huwelijk.features.marriage_intake.application;

import nl.rotterdam.huwelijk.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.CreateDossierDto;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.DossierSamenvattingDto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;

public interface MarriageIntakeService {

    long create(CreateDossierDto dto);

    void updateCeremonie(long dossierId, CeremonieSoort ceremonieSoort);

    DossierSamenvattingDto findById(long id);

    Set<LocalDate> findBeschikbareDatums(long dossierId, YearMonth maand);

    List<LocalTime> findBeschikbareTijden(long dossierId, LocalDate datum);

    void slaAfspraakOp(long dossierId, LocalDate datum, LocalTime startTijd);
}

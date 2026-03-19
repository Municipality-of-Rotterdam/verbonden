package nl.rotterdam.huwelijk.features.marriage_intake.application;

import nl.rotterdam.huwelijk.features.marriage_intake.domain.CreateDossierDto;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.DossierSamenvattingDto;

public interface MarriageIntakeService {

    long create(CreateDossierDto dto);

    DossierSamenvattingDto findById(long id);
}

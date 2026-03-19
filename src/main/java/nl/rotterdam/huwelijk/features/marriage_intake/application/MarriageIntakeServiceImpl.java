package nl.rotterdam.huwelijk.features.marriage_intake.application;

import nl.rotterdam.huwelijk.features.marriage_intake.domain.CreateDossierDto;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.DossierSamenvattingDto;
import nl.rotterdam.huwelijk.features.marriage_intake.repository.DossierRepository;
import nl.rotterdam.huwelijk.persistence.HuwelijksDossierEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class MarriageIntakeServiceImpl implements MarriageIntakeService {

    private final DossierRepository dossierRepository;

    MarriageIntakeServiceImpl(DossierRepository dossierRepository) {
        this.dossierRepository = dossierRepository;
    }

    @Override
    @Transactional
    public long create(CreateDossierDto dto) {
        HuwelijksDossierEntity entity = new HuwelijksDossierEntity();
        entity.setRegistratieType(dto.registratieType());
        entity.setCeremonieSoort(dto.ceremonieSoort());
        return dossierRepository.save(entity).getId();
    }

    @Override
    @Transactional(readOnly = true)
    public DossierSamenvattingDto findById(long id) {
        return dossierRepository.findById(id)
                .map(e -> new DossierSamenvattingDto(e.getId(), e.getRegistratieType(), e.getCeremonieSoort()))
                .orElseThrow(() -> new IllegalArgumentException("Dossier niet gevonden: " + id));
    }
}

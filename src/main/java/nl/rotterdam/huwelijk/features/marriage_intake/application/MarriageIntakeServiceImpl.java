package nl.rotterdam.huwelijk.features.marriage_intake.application;

import nl.rotterdam.huwelijk.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.CreateDossierDto;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.DossierSamenvattingDto;
import nl.rotterdam.huwelijk.features.marriage_intake.repository.DossierRepository;
import nl.rotterdam.huwelijk.persistence.HuwelijksDossierEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    @Transactional
    public void updateCeremonie(long dossierId, CeremonieSoort ceremonieSoort) {
        dossierRepository.findById(dossierId)
                .orElseThrow(() -> new IllegalArgumentException("Dossier niet gevonden: " + dossierId))
                .setCeremonieSoort(ceremonieSoort);
    }

    @Override
    @Transactional(readOnly = true)
    public DossierSamenvattingDto findById(long id) {
        return dossierRepository.findById(id)
                .map(e -> new DossierSamenvattingDto(
                        e.getId(),
                        e.getRegistratieType(),
                        e.getCeremonieSoort(),
                        null,
                        null,
                        false,
                        false,
                        List.of()))
                .orElseThrow(() -> new IllegalArgumentException("Dossier niet gevonden: " + id));
    }
}

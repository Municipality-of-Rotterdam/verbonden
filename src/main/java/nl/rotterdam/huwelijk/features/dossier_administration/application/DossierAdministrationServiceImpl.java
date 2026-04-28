package nl.rotterdam.huwelijk.features.dossier_administration.application;

import nl.rotterdam.huwelijk.features.dossier_administration.domain.ListDossierDto;
import nl.rotterdam.huwelijk.features.dossier_administration.repository.DossierAdministrationRepository;
import nl.rotterdam.huwelijk.persistence.HuwelijksDossierEntity;
import nl.rotterdam.huwelijk.persistence.HuwelijksDossiersPartnerEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
class DossierAdministrationServiceImpl implements DossierAdministrationService {

    private final DossierAdministrationRepository dossierAdministrationRepository;

    DossierAdministrationServiceImpl(DossierAdministrationRepository dossierAdministrationRepository) {
        this.dossierAdministrationRepository = dossierAdministrationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ListDossierDto> search(String zoekterm, Pageable pageable) {
        return dossierAdministrationRepository.search(zoekterm == null ? "" : zoekterm, pageable)
                .map(this::toListDossierDto);
    }

    @Override
    @Transactional(readOnly = true)
    public long count(String zoekterm) {
        return dossierAdministrationRepository.countByZoekterm(zoekterm == null ? "" : zoekterm);
    }

    private ListDossierDto toListDossierDto(HuwelijksDossierEntity entity) {
        List<HuwelijksDossiersPartnerEntity> sorted = entity.getPartners().stream()
                .sorted(Comparator.comparingInt(HuwelijksDossiersPartnerEntity::getVolgorde))
                .toList();
        String bsn1 = !sorted.isEmpty() ? sorted.get(0).getBsn() : null;
        String bsn2 = sorted.size() > 1 ? sorted.get(1).getBsn() : null;
        return new ListDossierDto(
                entity.getUuid(),
                bsn1,
                bsn2,
                entity.getRegistratieType(),
                entity.getCeremonieSoort(),
                entity.getAangemaaktOp());
    }
}

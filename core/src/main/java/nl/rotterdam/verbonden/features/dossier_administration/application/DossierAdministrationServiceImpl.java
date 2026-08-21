package nl.rotterdam.verbonden.features.dossier_administration.application;

import nl.rotterdam.verbonden.features.dossier_administration.domain.ListDossierDto;
import nl.rotterdam.verbonden.features.dossier_administration.repository.DossierAdministrationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class DossierAdministrationServiceImpl implements DossierAdministrationService {

    private final DossierAdministrationRepository dossierAdministrationRepository;

    DossierAdministrationServiceImpl(DossierAdministrationRepository dossierAdministrationRepository) {
        this.dossierAdministrationRepository = dossierAdministrationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ListDossierDto> search(String zoekterm, Pageable pageable) {
        return dossierAdministrationRepository.search(zoekterm == null ? "" : zoekterm, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public long count(String zoekterm) {
        return dossierAdministrationRepository.countByZoekterm(zoekterm == null ? "" : zoekterm);
    }
}

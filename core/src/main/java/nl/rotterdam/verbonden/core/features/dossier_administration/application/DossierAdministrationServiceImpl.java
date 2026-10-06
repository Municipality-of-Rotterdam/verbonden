package nl.rotterdam.verbonden.core.features.dossier_administration.application;

import nl.rotterdam.verbonden.core.features.dossier_administration.domain.ListDossierDto;
import nl.rotterdam.verbonden.core.features.dossier_administration.repository.DossierAdministrationRepository;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierNietWijzigbaarException;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierStatus;
import nl.rotterdam.verbonden.core.persistence.HuwelijksDossierEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

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

    @Override
    @Transactional
    public void accepteer(UUID dossierId) {
        beoordeel(dossierId, DossierStatus.GEACCEPTEERD);
    }

    @Override
    @Transactional
    public void wijsAf(UUID dossierId) {
        beoordeel(dossierId, DossierStatus.AFGEWEZEN);
    }

    private void beoordeel(UUID dossierId, DossierStatus nieuweStatus) {
        HuwelijksDossierEntity dossier = dossierAdministrationRepository.findByUuid(dossierId)
                .orElseThrow(() -> new IllegalArgumentException("Dossier niet gevonden: " + dossierId));
        if (dossier.getStatus() != DossierStatus.INGEDIEND) {
            throw new DossierNietWijzigbaarException(dossierId, dossier.getStatus(), DossierStatus.INGEDIEND);
        }
        dossier.setStatus(nieuweStatus);
    }
}

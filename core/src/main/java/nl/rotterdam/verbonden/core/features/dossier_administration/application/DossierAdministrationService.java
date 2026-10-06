package nl.rotterdam.verbonden.core.features.dossier_administration.application;

import nl.rotterdam.verbonden.core.features.dossier_administration.domain.ListDossierDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierNietWijzigbaarException;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface DossierAdministrationService {

    Page<ListDossierDto> search(String zoekterm, Pageable pageable);

    long count(String zoekterm);

    /**
     * Accepteert een ingediend dossier.
     *
     * @throws DossierNietWijzigbaarException wanneer het dossier niet de status {@link DossierStatus#INGEDIEND} heeft
     */
    void accepteer(UUID dossierId);

    /**
     * Wijst een ingediend dossier af.
     *
     * @throws DossierNietWijzigbaarException wanneer het dossier niet de status {@link DossierStatus#INGEDIEND} heeft
     */
    void wijsAf(UUID dossierId);
}

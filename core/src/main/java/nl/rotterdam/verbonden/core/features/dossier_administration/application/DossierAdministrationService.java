package nl.rotterdam.verbonden.core.features.dossier_administration.application;

import nl.rotterdam.verbonden.core.features.dossier_administration.domain.ListDossierDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DossierAdministrationService {

    Page<ListDossierDto> search(String zoekterm, Pageable pageable);

    long count(String zoekterm);
}

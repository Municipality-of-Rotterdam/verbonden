package nl.rotterdam.verbonden.core.features.betaling_administration.application;

import nl.rotterdam.verbonden.core.features.betaling_administration.domain.ListBetalingDto;
import nl.rotterdam.verbonden.core.features.betaling_administration.repository.BetalingAdministrationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
class BetalingAdministrationServiceImpl implements BetalingAdministrationService {

    private final BetalingAdministrationRepository repository;

    BetalingAdministrationServiceImpl(BetalingAdministrationRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ListBetalingDto> search(String zoekterm, Pageable pageable) {
        return repository.search(schoon(zoekterm), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public long count(String zoekterm) {
        return repository.count(schoon(zoekterm));
    }

    /** Een leeg zoekveld komt uit Wicket als {@code null}. */
    private static String schoon(String zoekterm) {
        return zoekterm == null ? "" : zoekterm.trim();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ListBetalingDto> findByDossierId(UUID dossierId) {
        return repository.findByDossierId(dossierId);
    }
}

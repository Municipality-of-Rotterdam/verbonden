package nl.rotterdam.huwelijk.features.baps_administration;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface BapsAdministrationService {

    Page<ListBapsDto> findAll(Pageable pageable);

    Optional<ChangeBapsDto> findById(Long id);

    ListBapsDto create(CreateBapsDto dto);

    ListBapsDto update(ChangeBapsDto dto);

    void toggleActief(Long id);

    void delete(Long id);

    long count();
}

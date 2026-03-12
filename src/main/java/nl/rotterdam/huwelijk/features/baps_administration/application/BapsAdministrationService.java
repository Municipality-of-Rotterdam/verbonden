package nl.rotterdam.huwelijk.features.baps_administration.application;

import nl.rotterdam.huwelijk.features.baps_administration.domain.ChangeBapsDto;
import nl.rotterdam.huwelijk.features.baps_administration.domain.CreateBapsDto;
import nl.rotterdam.huwelijk.features.baps_administration.domain.ListBapsDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface BapsAdministrationService {

    Page<ListBapsDto> findAll(Pageable pageable);

    Optional<ChangeBapsDto> findById(Long id);

    long create(CreateBapsDto dto);

    void update(ChangeBapsDto dto);

    void toggleActief(long id);

    void delete(long id);

    long count();
}

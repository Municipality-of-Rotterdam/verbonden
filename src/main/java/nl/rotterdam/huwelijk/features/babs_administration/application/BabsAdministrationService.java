package nl.rotterdam.huwelijk.features.babs_administration.application;

import nl.rotterdam.huwelijk.features.babs_administration.domain.ChangeBabsDto;
import nl.rotterdam.huwelijk.features.babs_administration.domain.CreateBabsDto;
import nl.rotterdam.huwelijk.features.babs_administration.domain.ListBabsDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface BabsAdministrationService {

    Page<ListBabsDto> findAll(Pageable pageable);

    Optional<ChangeBabsDto> findById(Long id);

    long create(CreateBabsDto dto);

    void update(ChangeBabsDto dto);

    void toggleActief(long id);

    long count();
}

package nl.rotterdam.huwelijk.features.location_administration.application;

import nl.rotterdam.huwelijk.features.location_administration.domain.ChangeBeschikbaarheidDto;
import nl.rotterdam.huwelijk.features.location_administration.domain.ChangeLocatieDto;
import nl.rotterdam.huwelijk.features.location_administration.domain.CreateBeschikbaarheidDto;
import nl.rotterdam.huwelijk.features.location_administration.domain.CreateLocatieDto;
import nl.rotterdam.huwelijk.features.location_administration.domain.ListBeschikbaarheidDto;
import nl.rotterdam.huwelijk.features.location_administration.domain.ListLocatieDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface LocationAdministrationService {

    Page<ListLocatieDto> findAll(Pageable pageable);

    Optional<ChangeLocatieDto> findById(long id);

    long create(CreateLocatieDto dto);

    void update(ChangeLocatieDto dto);

    void delete(long id);

    long count();

    List<ListBeschikbaarheidDto> findBeschikbaarheden(long locatieId);

    Optional<ChangeBeschikbaarheidDto> findBeschikbaarheidById(long id);

    long createBeschikbaarheid(CreateBeschikbaarheidDto dto);

    void updateBeschikbaarheid(ChangeBeschikbaarheidDto dto);

    void deleteBeschikbaarheid(long id);
}

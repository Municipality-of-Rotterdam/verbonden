package nl.rotterdam.verbonden.features.marriage_type_administration.application;

import nl.rotterdam.verbonden.features.marriage_type_administration.domain.ChangeMarriageTypeDto;
import nl.rotterdam.verbonden.features.marriage_type_administration.domain.CreateMarriageTypeDto;
import nl.rotterdam.verbonden.features.marriage_type_administration.domain.ListMarriageTypeDto;

import java.util.List;
import java.util.Optional;

public interface MarriageTypeAdministrationService {

    List<ListMarriageTypeDto> findAll();

    Optional<ChangeMarriageTypeDto> findById(long id);

    long create(CreateMarriageTypeDto dto);

    void update(ChangeMarriageTypeDto dto);

    void delete(long id);
}

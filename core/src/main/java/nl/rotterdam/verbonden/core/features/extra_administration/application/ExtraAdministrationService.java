package nl.rotterdam.verbonden.core.features.extra_administration.application;

import nl.rotterdam.verbonden.core.features.extra_administration.domain.ChangeExtraDto;
import nl.rotterdam.verbonden.core.features.extra_administration.domain.CreateExtraDto;
import nl.rotterdam.verbonden.core.features.extra_administration.domain.ListExtraDto;

import java.util.List;
import java.util.Optional;

public interface ExtraAdministrationService {

    List<ListExtraDto> findAll();

    Optional<ChangeExtraDto> findById(long id);

    long create(CreateExtraDto dto);

    void update(ChangeExtraDto dto);

    void delete(long id);

    long count();
}

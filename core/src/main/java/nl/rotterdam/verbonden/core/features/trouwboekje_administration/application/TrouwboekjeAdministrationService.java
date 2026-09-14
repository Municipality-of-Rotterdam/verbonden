package nl.rotterdam.verbonden.core.features.trouwboekje_administration.application;

import nl.rotterdam.verbonden.core.features.trouwboekje_administration.domain.ChangeTrouwboekjeDto;
import nl.rotterdam.verbonden.core.features.trouwboekje_administration.domain.CreateTrouwboekjeDto;
import nl.rotterdam.verbonden.core.features.trouwboekje_administration.domain.ListTrouwboekjeDto;

import java.util.List;
import java.util.Optional;

public interface TrouwboekjeAdministrationService {

    List<ListTrouwboekjeDto> findAll();

    Optional<ChangeTrouwboekjeDto> findById(long id);

    long create(CreateTrouwboekjeDto dto);

    void update(ChangeTrouwboekjeDto dto);

    void delete(long id);

    long count();
}

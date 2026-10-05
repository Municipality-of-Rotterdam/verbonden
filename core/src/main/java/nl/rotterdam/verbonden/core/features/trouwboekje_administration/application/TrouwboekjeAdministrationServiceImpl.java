package nl.rotterdam.verbonden.core.features.trouwboekje_administration.application;

import nl.rotterdam.verbonden.core.features.trouwboekje_administration.domain.ChangeTrouwboekjeDto;
import nl.rotterdam.verbonden.core.features.trouwboekje_administration.domain.CreateTrouwboekjeDto;
import nl.rotterdam.verbonden.core.features.trouwboekje_administration.domain.ListTrouwboekjeDto;
import nl.rotterdam.verbonden.core.features.trouwboekje_administration.repository.TrouwboekjeRepository;
import nl.rotterdam.verbonden.core.persistence.TrouwboekjeEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
class TrouwboekjeAdministrationServiceImpl implements TrouwboekjeAdministrationService {

    private final TrouwboekjeRepository trouwboekjeRepository;

    TrouwboekjeAdministrationServiceImpl(TrouwboekjeRepository trouwboekjeRepository) {
        this.trouwboekjeRepository = trouwboekjeRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ListTrouwboekjeDto> findAll() {
        return trouwboekjeRepository.findAll().stream()
                .map(e -> new ListTrouwboekjeDto(e.getId(), e.getNaam(), e.getAfbeelding(), e.getPrijs(), e.getStartdatum(), e.getEinddatum()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ChangeTrouwboekjeDto> findById(long id) {
        return trouwboekjeRepository.findById(id)
                .map(this::toChangeDto);
    }

    @Override
    @Transactional
    public long create(CreateTrouwboekjeDto dto) {
        TrouwboekjeEntity entity = new TrouwboekjeEntity();
        entity.setNaam(dto.naam());
        entity.setOmschrijving(dto.omschrijving());
        entity.setAfbeelding(dto.afbeelding());
        entity.setPrijs(dto.prijs());
        entity.setStartdatum(dto.startdatum());
        entity.setEinddatum(dto.einddatum());
        return trouwboekjeRepository.save(entity).getId();
    }

    @Override
    @Transactional
    public void update(ChangeTrouwboekjeDto dto) {
        TrouwboekjeEntity entity = trouwboekjeRepository.findById(dto.id())
                .orElseThrow(() -> new IllegalArgumentException("Trouwboekje niet gevonden: " + dto.id()));
        entity.setNaam(dto.naam());
        entity.setOmschrijving(dto.omschrijving());
        entity.setAfbeelding(dto.afbeelding());
        entity.setPrijs(dto.prijs());
        entity.setStartdatum(dto.startdatum());
        entity.setEinddatum(dto.einddatum());
        trouwboekjeRepository.save(entity);
    }

    @Override
    @Transactional
    public void delete(long id) {
        trouwboekjeRepository.findById(id).ifPresent(trouwboekje -> {
            trouwboekje.setActive(false);
            trouwboekjeRepository.save(trouwboekje);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return trouwboekjeRepository.count();
    }

    private ChangeTrouwboekjeDto toChangeDto(TrouwboekjeEntity e) {
        return new ChangeTrouwboekjeDto(e.getId(), e.getNaam(), e.getOmschrijving(),
                e.getAfbeelding(), e.getPrijs(), e.getStartdatum(), e.getEinddatum());
    }
}

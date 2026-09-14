package nl.rotterdam.verbonden.core.features.trouwboekje_administration.application;

import nl.rotterdam.verbonden.core.features.trouwboekje_administration.domain.ChangeTrouwboekjeDto;
import nl.rotterdam.verbonden.core.features.trouwboekje_administration.domain.CreateTrouwboekjeDto;
import nl.rotterdam.verbonden.core.features.extra.domain.ExtraType;
import nl.rotterdam.verbonden.core.features.trouwboekje_administration.domain.ListTrouwboekjeDto;
import nl.rotterdam.verbonden.core.features.extra.repository.ExtraRepository;
import nl.rotterdam.verbonden.core.persistence.ExtraEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
class TrouwboekjeAdministrationServiceImpl implements TrouwboekjeAdministrationService {

    private final ExtraRepository extraRepository;

    TrouwboekjeAdministrationServiceImpl(ExtraRepository extraRepository) {
        this.extraRepository = extraRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ListTrouwboekjeDto> findAll() {
        return extraRepository.findAll().stream()
                .filter(this::isTrouwboekje)
                .map(e -> new ListTrouwboekjeDto(e.getId(), e.getNaam(), e.getPrijs(), e.getStartdatum(), e.getEinddatum()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ChangeTrouwboekjeDto> findById(long id) {
        return extraRepository.findById(id)
                .filter(this::isTrouwboekje)
                .map(this::toChangeDto);
    }

    @Override
    @Transactional
    public long create(CreateTrouwboekjeDto dto) {
        ExtraEntity entity = new ExtraEntity();
        entity.setType(ExtraType.TROUWBOEKJE);
        entity.setNaam(dto.naam());
        entity.setOmschrijving(dto.omschrijving());
        entity.setAfbeelding(dto.afbeelding());
        entity.setPrijs(dto.prijs());
        entity.setStartdatum(dto.startdatum());
        entity.setEinddatum(dto.einddatum());
        return extraRepository.save(entity).getId();
    }

    @Override
    @Transactional
    public void update(ChangeTrouwboekjeDto dto) {
        ExtraEntity entity = extraRepository.findById(dto.id())
                .orElseThrow(() -> new IllegalArgumentException("Trouwboekje niet gevonden: " + dto.id()));
        if (!isTrouwboekje(entity)) {
            throw new IllegalArgumentException("Alleen trouwboekjes kunnen worden beheerd: " + dto.id());
        }
        entity.setType(ExtraType.TROUWBOEKJE);
        entity.setNaam(dto.naam());
        entity.setOmschrijving(dto.omschrijving());
        entity.setAfbeelding(dto.afbeelding());
        entity.setPrijs(dto.prijs());
        entity.setStartdatum(dto.startdatum());
        entity.setEinddatum(dto.einddatum());
        extraRepository.save(entity);
    }

    @Override
    @Transactional
    public void delete(long id) {
        extraRepository.findById(id).ifPresent(extra -> {
            extra.setActive(false);
            extraRepository.save(extra);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return extraRepository.countByType(ExtraType.TROUWBOEKJE);
    }

    private ChangeTrouwboekjeDto toChangeDto(ExtraEntity e) {
        return new ChangeTrouwboekjeDto(e.getId(), e.getNaam(), e.getOmschrijving(),
                e.getAfbeelding(), e.getPrijs(), e.getStartdatum(), e.getEinddatum());
    }

    private boolean isTrouwboekje(ExtraEntity entity) {
        return entity.getType() == ExtraType.TROUWBOEKJE;
    }
}

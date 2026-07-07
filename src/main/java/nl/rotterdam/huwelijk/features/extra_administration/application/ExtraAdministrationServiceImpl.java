package nl.rotterdam.huwelijk.features.extra_administration.application;

import nl.rotterdam.huwelijk.features.extra_administration.domain.ChangeExtraDto;
import nl.rotterdam.huwelijk.features.extra_administration.domain.CreateExtraDto;
import nl.rotterdam.huwelijk.features.extra_administration.domain.ListExtraDto;
import nl.rotterdam.huwelijk.features.extra_administration.repository.ExtraRepository;
import nl.rotterdam.huwelijk.persistence.ExtraEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
class ExtraAdministrationServiceImpl implements ExtraAdministrationService {

    private final ExtraRepository extraRepository;

    ExtraAdministrationServiceImpl(ExtraRepository extraRepository) {
        this.extraRepository = extraRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ListExtraDto> findAll() {
        return extraRepository.findAll().stream()
                .map(e -> new ListExtraDto(e.getId(), e.getType(), e.getNaam(), e.getPrijs(), e.getStartdatum(), e.getEinddatum()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ChangeExtraDto> findById(long id) {
        return extraRepository.findById(id).map(this::toChangeDto);
    }

    @Override
    @Transactional
    public long create(CreateExtraDto dto) {
        ExtraEntity entity = new ExtraEntity();
        entity.setType(dto.type());
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
    public void update(ChangeExtraDto dto) {
        ExtraEntity entity = extraRepository.findById(dto.id())
                .orElseThrow(() -> new IllegalArgumentException("Extra niet gevonden: " + dto.id()));
        entity.setType(dto.type());
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
        extraRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return extraRepository.count();
    }

    private ChangeExtraDto toChangeDto(ExtraEntity e) {
        return new ChangeExtraDto(e.getId(), e.getType(), e.getNaam(), e.getOmschrijving(),
                e.getAfbeelding(), e.getPrijs(), e.getStartdatum(), e.getEinddatum());
    }
}

package nl.rotterdam.huwelijk.features.marriage_type_administration.application;

import nl.rotterdam.huwelijk.features.marriage_type_administration.domain.ChangeMarriageTypeDto;
import nl.rotterdam.huwelijk.features.marriage_type_administration.domain.CreateMarriageTypeDto;
import nl.rotterdam.huwelijk.features.marriage_type_administration.domain.ListMarriageTypeDto;
import nl.rotterdam.huwelijk.features.marriage_type_administration.repository.MarriageTypeRepository;
import nl.rotterdam.huwelijk.persistence.MarriageTypeEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
class MarriageTypeAdministrationServiceImpl implements MarriageTypeAdministrationService {

    private final MarriageTypeRepository marriageTypeRepository;

    MarriageTypeAdministrationServiceImpl(MarriageTypeRepository marriageTypeRepository) {
        this.marriageTypeRepository = marriageTypeRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ListMarriageTypeDto> findAll() {
        return marriageTypeRepository.findAllProjected();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ChangeMarriageTypeDto> findById(long id) {
        return marriageTypeRepository.findById(id).map(this::toChangeDto);
    }

    @Override
    @Transactional
    public long create(CreateMarriageTypeDto dto) {
        MarriageTypeEntity entity = toEntity(dto);
        return marriageTypeRepository.save(entity).getId();
    }

    @Override
    @Transactional
    public void update(ChangeMarriageTypeDto dto) {
        MarriageTypeEntity entity = marriageTypeRepository.findById(dto.id())
                .orElseThrow(() -> new IllegalArgumentException("Huwelijkstype niet gevonden: " + dto.id()));
        entity.setTitel(dto.titel());
        entity.setTekst(dto.tekst());
        entity.setPrijs(dto.prijs());
        entity.setUrl(dto.url());
        entity.setSoort(dto.soort());
        marriageTypeRepository.save(entity);
    }

    @Override
    @Transactional
    public void delete(long id) {
        marriageTypeRepository.deleteById(id);
    }

    private ChangeMarriageTypeDto toChangeDto(MarriageTypeEntity entity) {
        return new ChangeMarriageTypeDto(
                entity.getId(),
                entity.getSoort(),
                entity.getTitel(),
                entity.getTekst(),
                entity.getPrijs(),
                entity.getUrl()
        );
    }

    private MarriageTypeEntity toEntity(CreateMarriageTypeDto dto) {
        MarriageTypeEntity entity = new MarriageTypeEntity();
        entity.setSoort(dto.soort());
        entity.setTitel(dto.titel());
        entity.setTekst(dto.tekst());
        entity.setPrijs(dto.prijs());
        entity.setUrl(dto.url());
        return entity;
    }
}

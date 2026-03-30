package nl.rotterdam.huwelijk.features.marriage_type_administration.application;

import nl.rotterdam.huwelijk.features.marriage_type_administration.domain.ChangeMarriageTypeDto;
import nl.rotterdam.huwelijk.features.marriage_type_administration.domain.CreateMarriageTypeDto;
import nl.rotterdam.huwelijk.features.marriage_type_administration.domain.ListMarriageTypeDto;
import nl.rotterdam.huwelijk.features.marriage_type_administration.repository.MarriageTypeLocationRepository;
import nl.rotterdam.huwelijk.features.marriage_type_administration.repository.MarriageTypeRepository;
import nl.rotterdam.huwelijk.persistence.MarriageTypeEntity;
import nl.rotterdam.huwelijk.persistence.MarriageTypeLocationEntity;
import nl.rotterdam.huwelijk.persistence.TrouwlocatieEntity;
import nl.rotterdam.huwelijk.features.location_administration.repository.LocatieRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
class MarriageTypeAdministrationServiceImpl implements MarriageTypeAdministrationService {

    private final MarriageTypeRepository marriageTypeRepository;
    private final MarriageTypeLocationRepository marriageTypeLocationRepository;
    private final LocatieRepository locatieRepository;

    MarriageTypeAdministrationServiceImpl(MarriageTypeRepository marriageTypeRepository,
                                          MarriageTypeLocationRepository marriageTypeLocationRepository,
                                          LocatieRepository locatieRepository) {
        this.marriageTypeRepository = marriageTypeRepository;
        this.marriageTypeLocationRepository = marriageTypeLocationRepository;
        this.locatieRepository = locatieRepository;
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
        MarriageTypeEntity saved = marriageTypeRepository.save(entity);
        if (dto.locatieId() != null) {
            TrouwlocatieEntity locatie = locatieRepository.findById(dto.locatieId())
                    .orElseThrow(() -> new IllegalArgumentException("Locatie niet gevonden: " + dto.locatieId()));
            MarriageTypeLocationEntity location = new MarriageTypeLocationEntity();
            location.setMarriageType(saved);
            location.setLocatie(locatie);
            marriageTypeLocationRepository.save(location);
        }
        return saved.getId();
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
        entity.setActive(dto.active());
        marriageTypeRepository.save(entity);

        marriageTypeLocationRepository.deleteByMarriageTypeId(entity.getId());
        if (dto.locatieId() != null) {
            TrouwlocatieEntity locatie = locatieRepository.findById(dto.locatieId())
                    .orElseThrow(() -> new IllegalArgumentException("Locatie niet gevonden: " + dto.locatieId()));
            MarriageTypeLocationEntity location = new MarriageTypeLocationEntity();
            location.setMarriageType(entity);
            location.setLocatie(locatie);
            marriageTypeLocationRepository.save(location);
        }
    }

    @Override
    @Transactional
    public void delete(long id) {
        marriageTypeRepository.deleteById(id);
    }

    private ChangeMarriageTypeDto toChangeDto(MarriageTypeEntity entity) {
        Long locatieId = marriageTypeLocationRepository.findById(entity.getId())
                .map(mtl -> mtl.getLocatie().getId())
                .orElse(null);
        return new ChangeMarriageTypeDto(
                entity.getId(),
                entity.getSoort(),
                entity.getTitel(),
                entity.getTekst(),
                entity.getPrijs(),
                entity.getUrl(),
                locatieId,
                entity.isActive()
        );
    }

    private MarriageTypeEntity toEntity(CreateMarriageTypeDto dto) {
        MarriageTypeEntity entity = new MarriageTypeEntity();
        entity.setSoort(dto.soort());
        entity.setTitel(dto.titel());
        entity.setTekst(dto.tekst());
        entity.setPrijs(dto.prijs());
        entity.setUrl(dto.url());
        entity.setActive(dto.active());
        return entity;
    }
}

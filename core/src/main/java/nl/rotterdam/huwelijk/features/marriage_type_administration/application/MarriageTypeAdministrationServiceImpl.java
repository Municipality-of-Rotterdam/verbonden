package nl.rotterdam.huwelijk.features.marriage_type_administration.application;

import nl.rotterdam.huwelijk.features.marriage_type_administration.domain.ChangeMarriageTypeDto;
import nl.rotterdam.huwelijk.features.marriage_type_administration.domain.CreateMarriageTypeDto;
import nl.rotterdam.huwelijk.features.marriage_type_administration.domain.ListMarriageTypeDto;
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
    private final LocatieRepository locatieRepository;

    MarriageTypeAdministrationServiceImpl(MarriageTypeRepository marriageTypeRepository,
                                          LocatieRepository locatieRepository) {
        this.marriageTypeRepository = marriageTypeRepository;
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
        if (dto.locatieId() != null) {
            TrouwlocatieEntity locatie = locatieRepository.findById(dto.locatieId())
                    .orElseThrow(() -> new IllegalArgumentException("Locatie niet gevonden: " + dto.locatieId()));
            MarriageTypeLocationEntity location = new MarriageTypeLocationEntity();
            location.setMarriageType(entity);
            location.setLocatie(locatie);
            entity.setLocation(location);
        }
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
        entity.setActive(dto.active());

        if (dto.locatieId() != null) {
            TrouwlocatieEntity locatie = locatieRepository.findById(dto.locatieId())
                    .orElseThrow(() -> new IllegalArgumentException("Locatie niet gevonden: " + dto.locatieId()));
            MarriageTypeLocationEntity existingLocation = entity.getLocation();
            if (existingLocation == null) {
                existingLocation = new MarriageTypeLocationEntity();
                existingLocation.setMarriageType(entity);
                entity.setLocation(existingLocation);
            }
            existingLocation.setLocatie(locatie);
        } else {
            entity.setLocation(null);
        }

        marriageTypeRepository.save(entity);
    }

    @Override
    @Transactional
    public void delete(long id) {
        marriageTypeRepository.deleteById(id);
    }

    private ChangeMarriageTypeDto toChangeDto(MarriageTypeEntity entity) {
        Long locatieId = entity.getLocation() != null
                ? entity.getLocation().getLocatie().getId()
                : null;
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

package nl.rotterdam.huwelijk.features.baps_administration.application;

import nl.rotterdam.huwelijk.features.baps_administration.domain.ChangeBapsDto;
import nl.rotterdam.huwelijk.features.baps_administration.domain.CreateBapsDto;
import nl.rotterdam.huwelijk.features.baps_administration.domain.ListBapsDto;
import nl.rotterdam.huwelijk.features.baps_administration.repository.BapsRepository;
import nl.rotterdam.huwelijk.persistence.BapsEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Optional;

@Service
class BapsAdministrationServiceImpl implements BapsAdministrationService {

    private final BapsRepository bapsRepository;

    BapsAdministrationServiceImpl(BapsRepository bapsRepository) {
        this.bapsRepository = bapsRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ListBapsDto> findAll(Pageable pageable) {
        return bapsRepository.findAllProjected(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ChangeBapsDto> findById(Long id) {
        return bapsRepository.findById(id).map(this::toChangeDto);
    }

    @Override
    @Transactional
    public long create(CreateBapsDto dto) {
        return bapsRepository.save(toEntity(dto)).getId();
    }

    @Override
    @Transactional
    public void update(ChangeBapsDto dto) {
        BapsEntity baps = bapsRepository.findById(dto.id())
                .orElseThrow(() -> new IllegalArgumentException("BAPS niet gevonden: " + dto.id()));
        baps.setNaam(dto.naam());
        baps.setFotoUrl(dto.fotoUrl());
        baps.setDetailUrl(dto.detailUrl());
        baps.setActief(dto.actief());
        baps.setActiefVanaf(dto.actiefVanaf());
        baps.setActiefTotEnMet(dto.actiefTotEnMet());
        baps.setBeschikbareDagen(dto.beschikbareDagen() != null
                ? new ArrayList<>(dto.beschikbareDagen()) : new ArrayList<>());
        bapsRepository.save(baps);
    }

    @Override
    @Transactional
    public void toggleActief(long id) {
        bapsRepository.toggleActief(id);
    }

    @Override
    public long count() {
        return bapsRepository.count();
    }

    private ChangeBapsDto toChangeDto(BapsEntity baps) {
        return new ChangeBapsDto(
                baps.getId(),
                baps.getNaam(),
                baps.getFotoUrl(),
                baps.getDetailUrl(),
                baps.isActief(),
                baps.getActiefVanaf(),
                baps.getActiefTotEnMet(),
                new ArrayList<>(baps.getBeschikbareDagen())
        );
    }

    private BapsEntity toEntity(CreateBapsDto dto) {
        BapsEntity baps = new BapsEntity();
        baps.setNaam(dto.naam());
        baps.setFotoUrl(dto.fotoUrl());
        baps.setDetailUrl(dto.detailUrl());
        baps.setActief(dto.actief());
        baps.setActiefVanaf(dto.actiefVanaf());
        baps.setActiefTotEnMet(dto.actiefTotEnMet());
        baps.setBeschikbareDagen(dto.beschikbareDagen() != null
                ? new ArrayList<>(dto.beschikbareDagen()) : new ArrayList<>());
        return baps;
    }
}

package nl.rotterdam.verbonden.core.features.babs_administration.application;

import nl.rotterdam.verbonden.core.features.babs_administration.domain.ChangeBabsDto;
import nl.rotterdam.verbonden.core.features.babs_administration.domain.CreateBabsDto;
import nl.rotterdam.verbonden.core.features.babs_administration.domain.ListBabsDto;
import nl.rotterdam.verbonden.core.features.babs_administration.repository.BabsRepository;
import nl.rotterdam.verbonden.core.persistence.BabsEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Optional;

@Service
class BabsAdministrationServiceImpl implements BabsAdministrationService {

    private final BabsRepository babsRepository;

    BabsAdministrationServiceImpl(BabsRepository babsRepository) {
        this.babsRepository = babsRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ListBabsDto> findAll(Pageable pageable) {
        return babsRepository.findAllProjected(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ChangeBabsDto> findById(Long id) {
        return babsRepository.findById(id).map(this::toChangeDto);
    }

    @Override
    @Transactional
    public long create(CreateBabsDto dto) {
        return babsRepository.save(toEntity(dto)).getId();
    }

    @Override
    @Transactional
    public void update(ChangeBabsDto dto) {
        BabsEntity babs = babsRepository.findById(dto.id())
                .orElseThrow(() -> new IllegalArgumentException("BABS niet gevonden: " + dto.id()));
        babs.setNaam(dto.naam());
        babs.setFotoUrl(dto.fotoUrl());
        babs.setDetailUrl(dto.detailUrl());
        babs.setActief(dto.actief());
        babs.setActiefVanaf(dto.actiefVanaf());
        babs.setActiefTotEnMet(dto.actiefTotEnMet());
        babs.setBeschikbareDagen(dto.beschikbareDagen() != null
                ? new ArrayList<>(dto.beschikbareDagen()) : new ArrayList<>());
        babsRepository.save(babs);
    }

    @Override
    @Transactional
    public void toggleActief(long id) {
        babsRepository.toggleActief(id);
    }

    @Override
    public long count() {
        return babsRepository.count();
    }

    private ChangeBabsDto toChangeDto(BabsEntity babs) {
        return new ChangeBabsDto(
                babs.getId(),
                babs.getNaam(),
                babs.getFotoUrl(),
                babs.getDetailUrl(),
                babs.isActief(),
                babs.getActiefVanaf(),
                babs.getActiefTotEnMet(),
                new ArrayList<>(babs.getBeschikbareDagen())
        );
    }

    private BabsEntity toEntity(CreateBabsDto dto) {
        BabsEntity babs = new BabsEntity();
        babs.setNaam(dto.naam());
        babs.setFotoUrl(dto.fotoUrl());
        babs.setDetailUrl(dto.detailUrl());
        babs.setActief(dto.actief());
        babs.setActiefVanaf(dto.actiefVanaf());
        babs.setActiefTotEnMet(dto.actiefTotEnMet());
        babs.setBeschikbareDagen(dto.beschikbareDagen() != null
                ? new ArrayList<>(dto.beschikbareDagen()) : new ArrayList<>());
        return babs;
    }
}

package nl.rotterdam.huwelijk.features.baps_administration;

import nl.rotterdam.huwelijk.persistence.BapsEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class BapsAdministrationServiceImpl implements BapsAdministrationService {

    private final BapsRepository bapsRepository;

    public BapsAdministrationServiceImpl(BapsRepository bapsRepository) {
        this.bapsRepository = bapsRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ListBapsDto> findAll(Pageable pageable) {
        return bapsRepository.findAll(pageable).map(this::toListDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ListBapsDto> findById(Long id) {
        return bapsRepository.findById(id).map(this::toListDto);
    }

    @Override
    @Transactional
    public ListBapsDto create(CreateBapsDto dto) {
        return toListDto(bapsRepository.save(toEntity(dto)));
    }

    @Override
    @Transactional
    public ListBapsDto update(ChangeBapsDto dto) {
        BapsEntity baps = bapsRepository.findById(dto.id())
                .orElseThrow(() -> new IllegalArgumentException("BAPS niet gevonden: " + dto.id()));
        baps.setNaam(dto.naam());
        baps.setFotoUrl(dto.fotoUrl());
        baps.setHobbies(dto.hobbies());
        baps.setBeschrijving(dto.beschrijving());
        baps.setActief(dto.actief());
        baps.setActiefVanaf(dto.actiefVanaf());
        baps.setActiefTotEnMet(dto.actiefTotEnMet());
        baps.setBeschikbareDagen(dto.beschikbareDagen() != null
                ? new ArrayList<>(dto.beschikbareDagen()) : new ArrayList<>());
        return toListDto(bapsRepository.save(baps));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        bapsRepository.deleteById(id);
    }

    @Override
    public long count() {
        return bapsRepository.count();
    }

    private ListBapsDto toListDto(BapsEntity baps) {
        return new ListBapsDto(
                baps.getId(),
                baps.getNaam(),
                baps.getFotoUrl(),
                baps.getHobbies(),
                baps.getBeschrijving(),
                baps.isActief(),
                baps.getActiefVanaf(),
                baps.getActiefTotEnMet(),
                new ArrayList<>(baps.getBeschikbareDagen()),
                baps.getAangemaaktOp()
        );
    }

    private BapsEntity toEntity(CreateBapsDto dto) {
        BapsEntity baps = new BapsEntity();
        baps.setNaam(dto.naam());
        baps.setFotoUrl(dto.fotoUrl());
        baps.setHobbies(dto.hobbies());
        baps.setBeschrijving(dto.beschrijving());
        baps.setActief(dto.actief());
        baps.setActiefVanaf(dto.actiefVanaf());
        baps.setActiefTotEnMet(dto.actiefTotEnMet());
        baps.setBeschikbareDagen(dto.beschikbareDagen() != null
                ? new ArrayList<>(dto.beschikbareDagen()) : new ArrayList<>());
        return baps;
    }
}

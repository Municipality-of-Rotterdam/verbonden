package nl.rotterdam.huwelijk.baps;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BapsServiceImpl implements BapsService {

    private final BapsRepository bapsRepository;

    public BapsServiceImpl(BapsRepository bapsRepository) {
        this.bapsRepository = bapsRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BapsDto> findAll() {
        return bapsRepository.findAllByOrderByNaamAsc().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BapsDto> findAll(Pageable pageable) {
        return bapsRepository.findAll(pageable).map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BapsDto> findById(Long id) {
        return bapsRepository.findById(id).map(this::toDto);
    }

    @Override
    @Transactional
    public BapsDto save(BapsDto dto) {
        return toDto(bapsRepository.save(toEntity(dto)));
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

    private BapsDto toDto(BapsEntity baps) {
        return new BapsDto(
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

    private BapsEntity toEntity(BapsDto dto) {
        BapsEntity baps = new BapsEntity();
        baps.setId(dto.id());
        baps.setNaam(dto.naam());
        baps.setFotoUrl(dto.fotoUrl());
        baps.setHobbies(dto.hobbies());
        baps.setBeschrijving(dto.beschrijving());
        baps.setActief(dto.actief());
        baps.setActiefVanaf(dto.actiefVanaf());
        baps.setActiefTotEnMet(dto.actiefTotEnMet());
        baps.setBeschikbareDagen(dto.beschikbareDagen() != null
                ? new ArrayList<>(dto.beschikbareDagen()) : new ArrayList<>());
        if (dto.aangemaaktOp() != null) {
            baps.setAangemaaktOp(dto.aangemaaktOp());
        }
        return baps;
    }
}

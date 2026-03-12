package nl.rotterdam.huwelijk.baps;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface BapsService {

    Page<ListBapsDto> findAll(Pageable pageable);

    Optional<ListBapsDto> findById(Long id);

    ListBapsDto create(CreateBapsDto dto);

    ListBapsDto update(ChangeBapsDto dto);

    void delete(Long id);

    long count();
}

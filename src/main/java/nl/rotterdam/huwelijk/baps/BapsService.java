package nl.rotterdam.huwelijk.baps;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface BapsService {

    Page<BapsDto> findAll(Pageable pageable);

    Optional<BapsDto> findById(Long id);

    BapsDto save(BapsDto dto);

    void delete(Long id);

    long count();
}

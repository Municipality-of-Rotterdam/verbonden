package nl.rotterdam.huwelijk.baps;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface BapsService {

    List<Baps> findAll();

    Page<Baps> findAll(Pageable pageable);

    Optional<Baps> findById(Long id);

    Baps save(Baps baps);

    void delete(Long id);

    long count();
}

package nl.rotterdam.huwelijk.baps;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BapsService {

    private final BapsRepository bapsRepository;

    public BapsService(BapsRepository bapsRepository) {
        this.bapsRepository = bapsRepository;
    }

    public List<Baps> findAll() {
        return bapsRepository.findAllByOrderByNaamAsc();
    }

    public Page<Baps> findAll(Pageable pageable) {
        return bapsRepository.findAll(pageable);
    }

    public Optional<Baps> findById(Long id) {
        return bapsRepository.findById(id);
    }

    @Transactional
    public Baps save(Baps baps) {
        return bapsRepository.save(baps);
    }

    @Transactional
    public void delete(Long id) {
        bapsRepository.deleteById(id);
    }

    public long count() {
        return bapsRepository.count();
    }
}

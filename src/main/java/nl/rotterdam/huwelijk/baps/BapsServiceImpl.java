package nl.rotterdam.huwelijk.baps;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BapsServiceImpl implements BapsService {

    private final BapsRepository bapsRepository;

    public BapsServiceImpl(BapsRepository bapsRepository) {
        this.bapsRepository = bapsRepository;
    }

    @Override
    public List<Baps> findAll() {
        return bapsRepository.findAllByOrderByNaamAsc();
    }

    @Override
    public Page<Baps> findAll(Pageable pageable) {
        return bapsRepository.findAll(pageable);
    }

    @Override
    public Optional<Baps> findById(Long id) {
        return bapsRepository.findById(id);
    }

    @Override
    @Transactional
    public Baps save(Baps baps) {
        return bapsRepository.save(baps);
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
}

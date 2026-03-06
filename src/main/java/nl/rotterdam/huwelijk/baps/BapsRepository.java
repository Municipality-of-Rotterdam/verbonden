package nl.rotterdam.huwelijk.baps;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BapsRepository extends JpaRepository<BapsEntity, Long> {

    List<BapsEntity> findAllByOrderByNaamAsc();

    Page<BapsEntity> findAll(Pageable pageable);
}

package nl.rotterdam.huwelijk.features.baps_administration.repository;

import nl.rotterdam.huwelijk.features.baps_administration.domain.ListBapsDto;
import nl.rotterdam.huwelijk.persistence.BapsEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BapsRepository extends JpaRepository<BapsEntity, Long> {

    @Query("""
            SELECT new nl.rotterdam.huwelijk.features.baps_administration.domain.ListBapsDto(
                b.id, b.naam, b.actief, b.actiefVanaf, b.actiefTotEnMet, b.aangemaaktOp)
            FROM BapsEntity b
            """)
    Page<ListBapsDto> findAllProjected(Pageable pageable);

    @Modifying
    @Query("UPDATE BapsEntity b SET b.actief = CASE WHEN b.actief = TRUE THEN FALSE ELSE TRUE END WHERE b.id = :id")
    void toggleActief(@Param("id") long id);
}

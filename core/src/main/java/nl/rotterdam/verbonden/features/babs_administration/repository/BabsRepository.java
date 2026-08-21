package nl.rotterdam.verbonden.features.babs_administration.repository;

import nl.rotterdam.verbonden.features.babs_administration.domain.ListBabsDto;
import nl.rotterdam.verbonden.persistence.BabsEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BabsRepository extends JpaRepository<BabsEntity, Long> {

    @Query("""
            SELECT new nl.rotterdam.verbonden.features.babs_administration.domain.ListBabsDto(
                b.id, b.naam, b.actief, b.actiefVanaf, b.actiefTotEnMet, b.aangemaaktOp)
            FROM BabsEntity b
            """)
    Page<ListBabsDto> findAllProjected(Pageable pageable);

    @Modifying
    @Query("UPDATE BabsEntity b SET b.actief = CASE WHEN b.actief = TRUE THEN FALSE ELSE TRUE END WHERE b.id = :id")
    void toggleActief(@Param("id") long id);
}

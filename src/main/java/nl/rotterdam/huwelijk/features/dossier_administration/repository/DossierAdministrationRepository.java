package nl.rotterdam.huwelijk.features.dossier_administration.repository;

import nl.rotterdam.huwelijk.persistence.HuwelijksDossierEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DossierAdministrationRepository extends JpaRepository<HuwelijksDossierEntity, Long> {

    @Query("""
            SELECT d FROM HuwelijksDossierEntity d
            WHERE :zoekterm IS NULL OR :zoekterm = ''
               OR EXISTS (SELECT p FROM HuwelijksDossiersPartnerEntity p
                          WHERE p.dossier = d AND p.bsn LIKE CONCAT('%', :zoekterm, '%'))
               OR LOWER(CAST(d.uuid AS String)) LIKE LOWER(CONCAT('%', :zoekterm, '%'))
            """)
    Page<HuwelijksDossierEntity> search(@Param("zoekterm") String zoekterm, Pageable pageable);

    @Query("""
            SELECT COUNT(d) FROM HuwelijksDossierEntity d
            WHERE :zoekterm IS NULL OR :zoekterm = ''
               OR EXISTS (SELECT p FROM HuwelijksDossiersPartnerEntity p
                          WHERE p.dossier = d AND p.bsn LIKE CONCAT('%', :zoekterm, '%'))
               OR LOWER(CAST(d.uuid AS String)) LIKE LOWER(CONCAT('%', :zoekterm, '%'))
            """)
    long countByZoekterm(@Param("zoekterm") String zoekterm);
}

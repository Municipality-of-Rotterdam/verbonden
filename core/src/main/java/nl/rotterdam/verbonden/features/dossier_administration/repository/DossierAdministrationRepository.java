package nl.rotterdam.verbonden.features.dossier_administration.repository;

import nl.rotterdam.verbonden.features.dossier_administration.domain.ListDossierDto;
import nl.rotterdam.verbonden.persistence.HuwelijksDossierEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DossierAdministrationRepository extends JpaRepository<HuwelijksDossierEntity, Long> {

    @Query(value = """
            SELECT new nl.rotterdam.verbonden.features.dossier_administration.domain.ListDossierDto(
                d.uuid,
                (SELECT p1.bsn FROM HuwelijksDossiersPartnerEntity p1 WHERE p1.dossier = d AND p1.volgorde = 1),
                (SELECT p2.bsn FROM HuwelijksDossiersPartnerEntity p2 WHERE p2.dossier = d AND p2.volgorde = 2),
                d.registratieType,
                d.ceremonieSoort,
                d.aangemaaktOp)
            FROM HuwelijksDossierEntity d
            WHERE :zoekterm IS NULL OR :zoekterm = ''
               OR EXISTS (SELECT p FROM HuwelijksDossiersPartnerEntity p
                          WHERE p.dossier = d
                            AND (p.bsn LIKE CONCAT('%', :zoekterm, '%')
                                 OR LOWER(p.gekozenAchternaam) LIKE LOWER(CONCAT('%', :zoekterm, '%'))))
               OR LOWER(CAST(d.uuid AS String)) LIKE LOWER(CONCAT('%', :zoekterm, '%'))
            """,
            countQuery = """
            SELECT COUNT(d) FROM HuwelijksDossierEntity d
            WHERE :zoekterm IS NULL OR :zoekterm = ''
               OR EXISTS (SELECT p FROM HuwelijksDossiersPartnerEntity p
                          WHERE p.dossier = d
                            AND (p.bsn LIKE CONCAT('%', :zoekterm, '%')
                                 OR LOWER(p.gekozenAchternaam) LIKE LOWER(CONCAT('%', :zoekterm, '%'))))
               OR LOWER(CAST(d.uuid AS String)) LIKE LOWER(CONCAT('%', :zoekterm, '%'))
            """)
    Page<ListDossierDto> search(@Param("zoekterm") String zoekterm, Pageable pageable);

    @Query("""
            SELECT COUNT(d) FROM HuwelijksDossierEntity d
            WHERE :zoekterm IS NULL OR :zoekterm = ''
               OR EXISTS (SELECT p FROM HuwelijksDossiersPartnerEntity p
                          WHERE p.dossier = d
                            AND (p.bsn LIKE CONCAT('%', :zoekterm, '%')
                                 OR LOWER(p.gekozenAchternaam) LIKE LOWER(CONCAT('%', :zoekterm, '%'))))
               OR LOWER(CAST(d.uuid AS String)) LIKE LOWER(CONCAT('%', :zoekterm, '%'))
            """)
    long countByZoekterm(@Param("zoekterm") String zoekterm);
}

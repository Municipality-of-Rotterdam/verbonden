package nl.rotterdam.verbonden.core.features.dossier_administration.repository;

import nl.rotterdam.verbonden.core.features.dossier_administration.domain.ListDossierNotitieDto;
import nl.rotterdam.verbonden.core.persistence.DossierNotitieEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DossierNotitieRepository extends JpaRepository<DossierNotitieEntity, Long> {

    @Query("""
            SELECT new nl.rotterdam.verbonden.core.features.dossier_administration.domain.ListDossierNotitieDto(
                n.id, n.tekst, n.gebruikersnaam, n.weergavenaam, n.aangemaaktOp)
            FROM DossierNotitieEntity n
            WHERE n.dossier.uuid = :dossierId
            ORDER BY n.aangemaaktOp DESC, n.id DESC
            """)
    List<ListDossierNotitieDto> findByDossierUuidOrderByAangemaaktOpDescIdDesc(
            @Param("dossierId") UUID dossierId);
}

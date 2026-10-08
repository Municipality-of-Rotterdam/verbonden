package nl.rotterdam.verbonden.core.features.dossier_administration.repository;

import nl.rotterdam.verbonden.core.features.dossier_administration.domain.ListDossierBestandDto;
import nl.rotterdam.verbonden.core.persistence.DossierBestandEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DossierBestandRepository extends JpaRepository<DossierBestandEntity, Long> {

    /**
     * De bestanden van een dossier, zonder hun inhoud te laden.
     */
    @Query("""
            SELECT new nl.rotterdam.verbonden.core.features.dossier_administration.domain.ListDossierBestandDto(
                b.id, b.bestandsnaam, b.bestandType, b.grootte, b.toegevoegdDoor, b.toegevoegdOp)
            FROM DossierBestandEntity b
            WHERE b.dossier.id = :dossierId
            ORDER BY b.toegevoegdOp, b.id
            """)
    List<ListDossierBestandDto> findOverzichtByDossierId(@Param("dossierId") long dossierId);

    Optional<DossierBestandEntity> findByIdAndDossier_Id(long id, long dossierId);
}

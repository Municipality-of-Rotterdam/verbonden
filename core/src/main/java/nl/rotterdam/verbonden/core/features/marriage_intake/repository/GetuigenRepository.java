package nl.rotterdam.verbonden.core.features.marriage_intake.repository;

import nl.rotterdam.verbonden.core.persistence.GetuigeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GetuigenRepository extends JpaRepository<GetuigeEntity, Long> {

    List<GetuigeEntity> findByDossier_IdOrderByVolgnummer(long dossierId);

    Optional<GetuigeEntity> findByDossier_IdAndVolgnummer(long dossierId, int volgnummer);

    @Modifying
    @Query("delete from GetuigeEntity g where g.dossier.id = :dossierId")
    void deleteByDossier_Id(long dossierId);

    long countByDossier_IdAndNaamIsNotNull(long dossierId);
}

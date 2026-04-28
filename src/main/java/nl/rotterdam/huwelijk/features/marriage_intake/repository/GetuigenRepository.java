package nl.rotterdam.huwelijk.features.marriage_intake.repository;

import nl.rotterdam.huwelijk.persistence.GetuigeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GetuigenRepository extends JpaRepository<GetuigeEntity, Long> {

    List<GetuigeEntity> findByDossier_IdOrderByVolgnummer(long dossierId);

    void deleteByDossier_Id(long dossierId);

    long countByDossier_IdAndNaamIsNotNull(long dossierId);
}

package nl.rotterdam.verbonden.core.features.marriage_intake.repository;

import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierStatus;
import nl.rotterdam.verbonden.core.persistence.AfspraakEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface AfspraakRepository extends JpaRepository<AfspraakEntity, Long> {

    Optional<AfspraakEntity> findFirstByDossier_Id(long dossierId);

    List<AfspraakEntity> findByLocatie_IdAndDatumAndDossier_StatusIn(long locatieId, LocalDate datum,
                                                                     Collection<DossierStatus> statussen);

    void deleteByDossier_Id(long dossierId);
}

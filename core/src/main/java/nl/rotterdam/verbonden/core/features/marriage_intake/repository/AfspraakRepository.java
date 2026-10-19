package nl.rotterdam.verbonden.core.features.marriage_intake.repository;

import nl.rotterdam.verbonden.core.features.marriage_intake.domain.BezetTijdslot;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierStatus;
import nl.rotterdam.verbonden.core.persistence.AfspraakEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface AfspraakRepository extends JpaRepository<AfspraakEntity, Long> {

    Optional<AfspraakEntity> findFirstByDossier_Id(long dossierId);

    @Query("""
            SELECT new nl.rotterdam.verbonden.core.features.marriage_intake.domain.BezetTijdslot(
                a.locatie.id, a.datum, a.startTijd, a.dossier.id)
            FROM AfspraakEntity a
            WHERE a.locatie.id IN :locatieIds
              AND a.datum BETWEEN :van AND :tot
              AND a.dossier.status IN :statussen
            """)
    List<BezetTijdslot> findBezetteTijdsloten(@Param("locatieIds") Collection<Long> locatieIds,
                                              @Param("van") LocalDate van,
                                              @Param("tot") LocalDate tot,
                                              @Param("statussen") Collection<DossierStatus> statussen);

    void deleteByDossier_Id(long dossierId);
}

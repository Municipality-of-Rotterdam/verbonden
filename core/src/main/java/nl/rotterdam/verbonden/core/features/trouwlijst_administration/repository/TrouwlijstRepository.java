package nl.rotterdam.verbonden.core.features.trouwlijst_administration.repository;

import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierStatus;
import nl.rotterdam.verbonden.core.persistence.AfspraakEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

@org.springframework.stereotype.Repository
public interface TrouwlijstRepository extends Repository<AfspraakEntity, Long> {

    @Query("""
            SELECT a FROM AfspraakEntity a
            JOIN FETCH a.locatie l
            JOIN FETCH a.dossier d
            WHERE a.datum BETWEEN :van AND :tot
              AND d.status = :status
            ORDER BY a.datum, l.naam, a.startTijd
            """)
    List<AfspraakEntity> findByDatumBetweenAndStatus(@Param("van") LocalDate van,
                                                     @Param("tot") LocalDate tot,
                                                     @Param("status") DossierStatus status);
}

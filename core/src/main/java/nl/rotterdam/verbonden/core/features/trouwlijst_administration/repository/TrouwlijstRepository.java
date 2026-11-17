package nl.rotterdam.verbonden.core.features.trouwlijst_administration.repository;

import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierStatus;
import nl.rotterdam.verbonden.core.features.trouwlijst_administration.domain.TrouwlijstAfspraakDto;
import nl.rotterdam.verbonden.core.persistence.AfspraakEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

@org.springframework.stereotype.Repository
public interface TrouwlijstRepository extends Repository<AfspraakEntity, Long> {

    @Query("""
            SELECT new nl.rotterdam.verbonden.core.features.trouwlijst_administration.domain.TrouwlijstAfspraakDto(
                d.uuid, a.datum, a.startTijd, l.naam, d.registratieType, d.ceremonieSoort)
            FROM AfspraakEntity a
            JOIN a.locatie l
            JOIN a.dossier d
            WHERE a.datum BETWEEN :van AND :tot
              AND d.status = :status
            ORDER BY a.datum, l.naam, a.startTijd
            """)
    List<TrouwlijstAfspraakDto> findByDatumBetweenAndStatus(@Param("van") LocalDate van,
                                                         @Param("tot") LocalDate tot,
                                                         @Param("status") DossierStatus status);
}

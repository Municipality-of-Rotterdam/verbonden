package nl.rotterdam.huwelijk.features.location_administration.repository;

import nl.rotterdam.huwelijk.features.location_administration.domain.ListNietBeschikbareDagDto;
import nl.rotterdam.huwelijk.persistence.LocatieNietBeschikbareDagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NietBeschikbareDagRepository extends JpaRepository<LocatieNietBeschikbareDagEntity, Long> {

    @Query("""
            SELECT new nl.rotterdam.huwelijk.features.location_administration.domain.ListNietBeschikbareDagDto(
                d.id, d.datum, d.reden, d.laatsteWijzigDatum, d.userid)
            FROM LocatieNietBeschikbareDagEntity d
            WHERE d.locatie.id = :locatieId
            ORDER BY d.datum
            """)
    List<ListNietBeschikbareDagDto> findByLocatieId(@Param("locatieId") long locatieId);
}

package nl.rotterdam.verbonden.features.location_administration.repository;

import nl.rotterdam.verbonden.features.location_administration.domain.ListNietBeschikbareDagDto;
import nl.rotterdam.verbonden.persistence.LocatieNietBeschikbareDagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Repository
public interface NietBeschikbareDagRepository extends JpaRepository<LocatieNietBeschikbareDagEntity, Long> {

    @Query("""
            SELECT new nl.rotterdam.verbonden.features.location_administration.domain.ListNietBeschikbareDagDto(
                d.id, d.datum, d.reden, d.laatsteWijzigDatum, d.userid)
            FROM LocatieNietBeschikbareDagEntity d
            WHERE d.locatie.id = :locatieId
            ORDER BY d.datum
            """)
    List<ListNietBeschikbareDagDto> findByLocatieId(@Param("locatieId") long locatieId);

    @Query("SELECT d.datum FROM LocatieNietBeschikbareDagEntity d WHERE d.locatie.id = :locatieId")
    Set<LocalDate> findDatumsByLocatieId(@Param("locatieId") long locatieId);

    boolean existsByLocatie_IdAndDatum(long locatieId, LocalDate datum);
}

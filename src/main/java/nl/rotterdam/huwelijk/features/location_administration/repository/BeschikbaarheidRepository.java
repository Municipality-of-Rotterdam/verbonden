package nl.rotterdam.huwelijk.features.location_administration.repository;

import nl.rotterdam.huwelijk.features.location_administration.domain.ListBeschikbaarheidDto;
import nl.rotterdam.huwelijk.persistence.LocatieBeschikbaarheidEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BeschikbaarheidRepository extends JpaRepository<LocatieBeschikbaarheidEntity, Long> {

    @Query("""
            SELECT new nl.rotterdam.huwelijk.features.location_administration.domain.ListBeschikbaarheidDto(
                b.id, b.huwelijkstype, b.dagVanDeWeek, b.startTijd, b.eindTijd,
                b.duurInMinuten, b.prijs, b.ingangsdatum, b.einddatum)
            FROM LocatieBeschikbaarheidEntity b
            WHERE b.locatie.id = :locatieId
            ORDER BY b.huwelijkstype, b.dagVanDeWeek, b.startTijd
            """)
    List<ListBeschikbaarheidDto> findByLocatieId(@Param("locatieId") long locatieId);
}

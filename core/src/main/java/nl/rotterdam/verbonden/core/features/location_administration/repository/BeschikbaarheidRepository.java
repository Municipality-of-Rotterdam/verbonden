package nl.rotterdam.verbonden.core.features.location_administration.repository;

import nl.rotterdam.verbonden.core.features.location_administration.domain.HuwelijksType;
import nl.rotterdam.verbonden.core.features.location_administration.domain.ListBeschikbaarheidDto;
import nl.rotterdam.verbonden.core.persistence.LocatieBeschikbaarheidEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface BeschikbaarheidRepository extends JpaRepository<LocatieBeschikbaarheidEntity, Long> {

    @Query("""
            SELECT new nl.rotterdam.verbonden.core.features.location_administration.domain.ListBeschikbaarheidDto(
                b.id, b.huwelijkstype, b.dagVanDeWeek, b.startTijd, b.eindTijd,
                b.duurInMinuten, b.prijs, b.ingangsdatum, b.einddatum)
            FROM LocatieBeschikbaarheidEntity b
            WHERE b.locatie.id = :locatieId
            ORDER BY b.dagVanDeWeek, b.startTijd, b.ingangsdatum
            """)
    List<ListBeschikbaarheidDto> findByLocatieId(@Param("locatieId") long locatieId);

    @Query("""
            SELECT b FROM LocatieBeschikbaarheidEntity b
            WHERE b.locatie.id = :locatieId
              AND b.huwelijkstype = :huwelijkstype
              AND b.dagVanDeWeek = :dag
              AND b.ingangsdatum <= :datum
              AND b.einddatum >= :datum
            """)
    List<LocatieBeschikbaarheidEntity> findBeschikbareSlots(
            @Param("locatieId") long locatieId,
            @Param("huwelijkstype") HuwelijksType huwelijkstype,
            @Param("dag") DayOfWeek dag,
            @Param("datum") LocalDate datum);
}

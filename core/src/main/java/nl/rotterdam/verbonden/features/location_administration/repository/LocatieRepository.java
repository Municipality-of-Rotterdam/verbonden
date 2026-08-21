package nl.rotterdam.verbonden.features.location_administration.repository;

import nl.rotterdam.verbonden.features.location_administration.domain.ListLocatieDto;
import nl.rotterdam.verbonden.persistence.TrouwlocatieEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocatieRepository extends JpaRepository<TrouwlocatieEntity, Long> {

    @Query("""
            SELECT new nl.rotterdam.verbonden.features.location_administration.domain.ListLocatieDto(
                l.id, l.naam, l.fotoUrl, l.omschrijving, l.detailUrl)
            FROM TrouwlocatieEntity l
            """)
    Page<ListLocatieDto> findAllProjected(Pageable pageable);

    @Query("""
            SELECT new nl.rotterdam.verbonden.features.location_administration.domain.ListLocatieDto(
                l.id, l.naam, l.fotoUrl, l.omschrijving, l.detailUrl)
            FROM TrouwlocatieEntity l
            ORDER BY l.naam ASC
            """)
    List<ListLocatieDto> findAllProjectedUnpaged();
}

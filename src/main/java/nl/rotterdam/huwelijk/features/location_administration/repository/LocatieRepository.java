package nl.rotterdam.huwelijk.features.location_administration.repository;

import nl.rotterdam.huwelijk.features.location_administration.domain.ListLocatieDto;
import nl.rotterdam.huwelijk.persistence.TrouwlocatieEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface LocatieRepository extends JpaRepository<TrouwlocatieEntity, Long> {

    @Query("""
            SELECT new nl.rotterdam.huwelijk.features.location_administration.domain.ListLocatieDto(
                l.id, l.naam, l.fotoUrl)
            FROM TrouwlocatieEntity l
            """)
    Page<ListLocatieDto> findAllProjected(Pageable pageable);
}
